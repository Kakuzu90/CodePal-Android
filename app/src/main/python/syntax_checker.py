import ast

def check(code):
    try:
        ast.parse(code)
        return {"status": "ok"}
    except SyntaxError as e:
        return {
            "status": "error",
            "line": e.lineno,
            "offset": e.offset,
            "msg": e.msg,
            "text": e.text
        }
