import sys, cgi, re, operator, random
from BaseHTTPServer import BaseHTTPRequestHandler,HTTPServer

REST_STATE = {}

class MyServer(BaseHTTPRequestHandler):
    def do_GET(self):
        global REST_STATE

        params = {}
        if self.path.find("?") != -1:
            params = cgi.parse_qs( self.path.split("?")[1] )
        print params
        print self.headers
        if "q" in params:
            head, body = solve(params, self.headers) 
            self.send_response(200, 'OK')
            self.send_header('Content-type', 'text/html')
            for [k, v] in head:
                self.send_header(k, v)
            self.end_headers()
            self.wfile.write( body )
        else:
            if self.path in REST_STATE:
                self.send_response(200, 'OK')
                self.send_header('Content-type', 'text/html')
                self.end_headers()
                self.wfile.write( REST_STATE[self.path] )
            else:
                self.send_response(404, 'Not Found')
                self.send_header('Content-type', 'text/html')
                self.end_headers()
                self.wfile.write("Not Found")

    def do_DELETE(self):
        global REST_STATE
        self.send_response(200, 'OK')
        self.send_header('Content-type', 'text/html')
        self.end_headers()
        del REST_STATE[self.path]
            
    def do_PUT(self):
        global REST_STATE
        length = int(self.headers.getheader('content-length'))
        data = self.rfile.read(length)
        
        REST_STATE[self.path] = data
        print "Got", data, self.path
        
        self.send_response(200, 'OK')
        self.send_header('Content-type', 'text/html')
        self.end_headers()
        self.wfile.write( "" )
        

    @staticmethod
    def serve_forever(port):
        HTTPServer(('', port), MyServer).serve_forever()

def largest_number(q):
    nums = [int(x) for x in q[q.index(":") + 1:].split(", ")]
    return max(nums)

def arithm_params(q, params):
    ops = { "+": operator.add, "-": operator.sub, "*": operator.mul }
    m = re.search("is ([a-z]+) ([\+*-]) ([a-z]+)", q)
    p1 = int(params[m.group(1)][0])
    p2 = int(params[m.group(3)][0])
    return ops[m.group(2)](p1, p2)

def arithm(q):
    ops = { "+": operator.add, "-": operator.sub, "*": operator.mul }
    m = re.search("is ([0-9]+) ([\+*-]) ([0-9]+)", q)
    return ops[m.group(2)](int(m.group(1)), int(m.group(3)))

def referer(h):
    ref = h.get("Referer")
    return ref

SESSION_ID = 1

def sid():
    global SESSION_ID
    SESSION_ID+=1
    return str(SESSION_ID)

def sessioncookie(h):
    if h.get("Cookie"):
        return h.get("Cookie").split(";")[0].split("=")[1]
    return None


def solve(params, headers):
    if "q" not in params:
        return NAME
    global SESSIONS
    out = []
    result = NAME
    
    q = params["q"][0]
    if q.find("largest") != -1:
        # Which of the numbers is largest: 841, 973, 279, 146, 923
        result = largest_number(q)
    elif re.search("How much is [a-z] [\+*-] [a-z]", q):
        result = arithm_params(q, params)
    elif re.search("How much is [0-9]+ [\+*-] [0-9]+", q):
        result = arithm(q)
    elif q.find("Which page am I coming from") != -1:
        result = referer(headers)
    elif q.find("My name is") != -1:
        name = re.search("My name is (\w+)\.", q).group(1)
        s = sid()
        SESSIONS[s] = name
        out.append(["Set-Cookie", "SID=%s; domain=localhost" % s])
        result = name
    elif q.find("What is my name") != -1:
        s = sessioncookie(headers)
        print "Got sid", s
        name = SESSIONS[s]
        result = name
    elif re.search("Let \w+ be \w+", q):
        name, value = re.search("Let (\w+) be (\w+)\.", q).groups()
        s = sessioncookie(headers)
        if not s:
            s = sid()
        print "Got sid", s
        if s in SESSIONS:
            vals = SESSIONS[s]
        else:
            vals = {}
        vals[name] = int(value)
        SESSIONS[s] = vals
        print "Session: ", vals
        
        out.append(["Set-Cookie", "SID=%s; domain=localhost" % s])
        result = value
    elif q.find("Remember how much is") != -1:
        n1, op, n2 = re.search("Remember how much is (\w+) ([+*-]) (\w+)", q).groups()
        s = sessioncookie(headers)
        print "Got sid", s
        vals = SESSIONS[s]
        print "Session: ", vals
        ops = { "+": operator.add, "-": operator.sub, "*": operator.mul }
        result = ops[op](vals[n1], vals[n2])
        
    return (out, result)

if __name__ == "__main__":
    NAME=sys.argv[2]
    SESSIONS={}
    MyServer.serve_forever(int(sys.argv[1]))
    print largest_number("Which of the numbers is largest: 841, 973, 279, 146, 923")
