import sys, cgi, re, operator
from BaseHTTPServer import BaseHTTPRequestHandler,HTTPServer

REST_STATE = {}

class MyServer(BaseHTTPRequestHandler):
    def do_GET(self):
        # import time
        # time.sleep(10)
        params = {}
        if self.path.find("?") != -1:
            params = cgi.parse_qs( self.path.split("?")[1] )
        print params
        print self.headers
        if "q" in params:
            self.send_response(200, 'OK')
            self.send_header('Content-type', 'text/html')
            self.end_headers()
            self.wfile.write( solve(params, self.headers) )
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
        self.send_response(200, 'OK')
        self.send_header('Content-type', 'text/html')
        self.end_headers()
        del REST_STATE[self.path]
            
    def do_PUT(self):
        length = int(self.headers.getheader('content-length'))
        data = self.rfile.read(length)
        
        REST_STATE[self.path] = data
        print "Got", data
        
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

def solve(params, headers):
    if "q" not in params:
        return NAME
    
    q = params["q"][0]
    if q.find("largest") != -1:
        # Which of the numbers is largest: 841, 973, 279, 146, 923
        return largest_number(q)
    elif re.search("[a-z] [\+*-] [a-z]", q):
        return arithm_params(q, params)
    elif re.search("[0-9]+ [\+*-] [0-9]+", q):
        return arithm(q)
    elif q.find("Which page am I coming from") != -1:
        return referer(headers)
    return NAME

if __name__ == "__main__":
    NAME=sys.argv[2]
    MyServer.serve_forever(int(sys.argv[1]))
    print largest_number("Which of the numbers is largest: 841, 973, 279, 146, 923")
