import ast

def check(code):
    try:
        # Use compile() for better validation than ast.parse()
        compile(code, '<string>', 'exec')
        
        # Additional semantic checks
        tree = ast.parse(code)
        
        # Check for empty code
        if len(tree.body) == 0:
            return {
                "status": "error",
                "line": 1,
                "offset": 0,
                "msg": "Empty code block",
                "text": ""
            }
        
        # Check if it's just a single identifier (like 'k')
        if (len(tree.body) == 1 and 
            isinstance(tree.body[0], ast.Expr) and 
            isinstance(tree.body[0].value, ast.Name)):
            return {
                "status": "error", 
                "line": 1,
                "offset": 0,
                "msg": "Incomplete code: just a variable name",
                "text": code.strip()
            }
        
        return {"status": "ok"}
    except SyntaxError as e:
        return {
            "status": "error",
            "line": e.lineno,
            "offset": e.offset,
            "msg": e.msg,
            "text": e.text
        }
    except Exception as e:
        return {
            "status": "error",
            "line": 1,
            "offset": 0,
            "msg": str(e),
            "text": code.strip()
        }
