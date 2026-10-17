"""Cheap brace/paren balance check for Kotlin files (strings and comments stripped)."""
import sys


def strip_kotlin(src):
    out = []
    i = 0
    n = len(src)
    while i < n:
        c = src[i]
        if c == '/' and i + 1 < n and src[i + 1] == '/':
            j = src.find('\n', i)
            i = n if j < 0 else j
        elif c == '/' and i + 1 < n and src[i + 1] == '*':
            j = src.find('*/', i + 2)
            i = n if j < 0 else j + 2
        elif src.startswith('"""', i):
            j = src.find('"""', i + 3)
            i = n if j < 0 else j + 3
        elif c == '"':
            i += 1
            while i < n:
                if src[i] == '\\':
                    i += 2
                    continue
                if src[i] == '"':
                    i += 1
                    break
                i += 1
        elif c == "'":
            i += 1
            while i < n:
                if src[i] == '\\':
                    i += 2
                    continue
                if src[i] == "'":
                    i += 1
                    break
                i += 1
        else:
            out.append(c)
            i += 1
    return ''.join(out)


def kontrol(p):
    s = strip_kotlin(open(p, encoding='utf-8').read())
    stack = []
    pairs = {'}': '{', ')': '(', ']': '['}
    for idx, ch in enumerate(s):
        if ch in '{([':
            stack.append(ch)
        elif ch in '})]':
            if not stack or stack[-1] != pairs[ch]:
                return f"BAD at line {s.count(chr(10), 0, idx) + 1}"
            stack.pop()
    return 'ok' if not stack else f"unclosed {''.join(stack)}"


if __name__ == '__main__':
    hatali = 0
    for yol in sys.argv[1:]:
        sonuc = kontrol(yol)
        if sonuc != 'ok':
            hatali += 1
        print(f"{yol}: {sonuc}")
    sys.exit(1 if hatali else 0)
