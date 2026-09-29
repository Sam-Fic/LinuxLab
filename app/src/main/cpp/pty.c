/*
 * Linux 入门 —— Linux 命令学习与真实终端 App
 * Copyright (C) 2026 拾星*
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 * 原生 PTY：forkpty + exec，让 App 里跑起来的是真正的进程（和 Termux 同一套做法）。
 * 没有 PTY 就没有作业控制、没有 Ctrl+C、vi/top/python REPL 也跑不起来。
 */
#include <jni.h>
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <unistd.h>
#include <fcntl.h>
#include <errno.h>
#include <signal.h>
#include <pty.h>
#include <sys/ioctl.h>
#include <sys/wait.h>
#include <termios.h>

#define MAX_SESSIONS 16

typedef struct {
    int fd;
    pid_t pid;
    int used;
} Session;

static Session g_sessions[MAX_SESSIONS];

static void session_put(int fd, pid_t pid) {
    for (int i = 0; i < MAX_SESSIONS; i++) {
        if (!g_sessions[i].used) {
            g_sessions[i].fd = fd;
            g_sessions[i].pid = pid;
            g_sessions[i].used = 1;
            return;
        }
    }
}

static pid_t session_pid(int fd) {
    for (int i = 0; i < MAX_SESSIONS; i++) {
        if (g_sessions[i].used && g_sessions[i].fd == fd) return g_sessions[i].pid;
    }
    return -1;
}

static void session_remove(int fd) {
    for (int i = 0; i < MAX_SESSIONS; i++) {
        if (g_sessions[i].used && g_sessions[i].fd == fd) {
            g_sessions[i].used = 0;
            g_sessions[i].fd = -1;
            g_sessions[i].pid = -1;
        }
    }
}

static char **to_string_array(JNIEnv *env, jobjectArray arr) {
    if (arr == NULL) {
        char **out = calloc(1, sizeof(char *));
        out[0] = NULL;
        return out;
    }
    jsize len = (*env)->GetArrayLength(env, arr);
    char **out = calloc((size_t) len + 1, sizeof(char *));
    for (jsize i = 0; i < len; i++) {
        jstring js = (jstring) (*env)->GetObjectArrayElement(env, arr, i);
        if (js == NULL) { out[i] = strdup(""); continue; }
        const char *s = (*env)->GetStringUTFChars(env, js, NULL);
        out[i] = strdup(s);
        (*env)->ReleaseStringUTFChars(env, js, s);
        (*env)->DeleteLocalRef(env, js);
    }
    out[len] = NULL;
    return out;
}

static void free_string_array(char **arr) {
    if (arr == NULL) return;
    for (int i = 0; arr[i] != NULL; i++) free(arr[i]);
    free(arr);
}

JNIEXPORT jint JNICALL
Java_com_linuxlab_starter_terminal_pty_PtyNative_createPty(
        JNIEnv *env, jclass clazz,
        jobjectArray command, jobjectArray environment, jstring cwd,
        jint rows, jint cols) {

    char **argv = to_string_array(env, command);
    char **envp = to_string_array(env, environment);
    const char *c_path = cwd ? (*env)->GetStringUTFChars(env, cwd, NULL) : NULL;

    struct winsize ws;
    memset(&ws, 0, sizeof(ws));
    ws.ws_row = (unsigned short) rows;
    ws.ws_col = (unsigned short) cols;
    ws.ws_xpixel = 0;
    ws.ws_ypixel = 0;

    int master = -1;
    pid_t pid = forkpty(&master, NULL, NULL, &ws);

    if (pid < 0) {
        free_string_array(argv);
        free_string_array(envp);
        if (c_path) (*env)->ReleaseStringUTFChars(env, cwd, c_path);
        return -1;
    }

    if (pid == 0) {
        // ---- 子进程 ----
        if (c_path) chdir(c_path);
        if (c_path) (*env)->ReleaseStringUTFChars(env, cwd, c_path);

        // 新进程组：让 Ctrl+C 之类的信号由终端投递给前台任务
        setpgid(0, 0);
        signal(SIGPIPE, SIG_DFL);
        signal(SIGTTOU, SIG_DFL);
        signal(SIGTTIN, SIG_DFL);

        execve(argv[0], argv, envp);
        // exec 失败：把具体原因写回终端，方便在界面上直接看到
        const char *prefix = "\r\n[exec 失败] ";
        const char *err = strerror(errno);
        const char *suffix = "\r\n";
        ssize_t ignored = write(2, prefix, strlen(prefix));
        ignored = write(2, argv[0], strlen(argv[0]));
        const char *mid = ": ";
        ignored = write(2, mid, strlen(mid));
        ignored = write(2, err, strlen(err));
        ignored = write(2, suffix, strlen(suffix));
        (void) ignored;
        _exit(127);
    }

    // ---- 父进程 ----
    if (c_path) (*env)->ReleaseStringUTFChars(env, cwd, c_path);
    free_string_array(argv);
    free_string_array(envp);

    int flags = fcntl(master, F_GETFL, 0);
    fcntl(master, F_SETFL, flags | O_NONBLOCK);
    session_put(master, pid);
    return master;
}

JNIEXPORT jint JNICALL
Java_com_linuxlab_starter_terminal_pty_PtyNative_writePty(
        JNIEnv *env, jclass clazz, jint fd, jbyteArray data, jint length) {
    jbyte *buf = (*env)->GetByteArrayElements(env, data, NULL);
    if (buf == NULL) return -1;
    ssize_t n = write(fd, buf, (size_t) length);
    (*env)->ReleaseByteArrayElements(env, data, buf, JNI_ABORT);
    return (jint) n;
}

JNIEXPORT jint JNICALL
Java_com_linuxlab_starter_terminal_pty_PtyNative_readPty(
        JNIEnv *env, jclass clazz, jint fd, jbyteArray buffer) {
    jsize capacity = (*env)->GetArrayLength(env, buffer);
    jbyte *buf = (*env)->GetByteArrayElements(env, buffer, NULL);
    if (buf == NULL) return -1;
    ssize_t n = read(fd, buf, (size_t) capacity);
    if (n > 0) {
        (*env)->ReleaseByteArrayElements(env, buffer, buf, 0);
        return (jint) n;
    }
    (*env)->ReleaseByteArrayElements(env, buffer, buf, JNI_ABORT);
    if (n == 0) return 0;                       // EOF：进程已退出
    if (errno == EAGAIN || errno == EWOULDBLOCK) return -2;
    return -1;
}

JNIEXPORT void JNICALL
Java_com_linuxlab_starter_terminal_pty_PtyNative_resizePty(
        JNIEnv *env, jclass clazz, jint fd, jint rows, jint cols) {
    struct winsize ws;
    memset(&ws, 0, sizeof(ws));
    ws.ws_row = (unsigned short) rows;
    ws.ws_col = (unsigned short) cols;
    ioctl(fd, TIOCSWINSZ, &ws);
    pid_t pid = session_pid(fd);
    if (pid > 0) kill(pid, SIGWINCH);
}

JNIEXPORT jint JNICALL
Java_com_linuxlab_starter_terminal_pty_PtyNative_waitPty(
        JNIEnv *env, jclass clazz, jint fd) {
    pid_t pid = session_pid(fd);
    if (pid <= 0) return -1;
    int status = 0;
    pid_t r = waitpid(pid, &status, WNOHANG);
    if (r == 0) return -2;                      // 仍在运行
    if (r < 0) return -1;
    return WIFEXITED(status) ? WEXITSTATUS(status) : 128 + WTERMSIG(status);
}

JNIEXPORT void JNICALL
Java_com_linuxlab_starter_terminal_pty_PtyNative_closePty(
        JNIEnv *env, jclass clazz, jint fd) {
    pid_t pid = session_pid(fd);
    if (pid > 0) {
        kill(-pid, SIGHUP);
        kill(pid, SIGKILL);
        int status;
        waitpid(pid, &status, 0);
    }
    close(fd);
    session_remove(fd);
}
