import sys, cgi, re
from BaseHTTPServer import BaseHTTPRequestHandler,HTTPServer

class MyServer(BaseHTTPRequestHandler):
    def do_GET(self):
        # import time
        # time.sleep(10)
        params = cgi.parse_qs( self.path.split("?")[1] )
        print params
        self.send_response(200, 'OK')
        self.send_header('Content-type', 'text/html')
        self.end_headers()
        self.wfile.write( solve(params) )

    @staticmethod
    def serve_forever(port):
        HTTPServer(('', port), MyServer).serve_forever()

def largest_number(q):
    nums = [int(x) for x in q[q.index(":") + 1:].split(", ")]
    return max(nums)
   
def sum(q):
    m = re.search("is ([0-9]+) \+ ([0-9]+)", q)
    return int(m.group(1)) + int(m.group(2))

def solve(params):
    q = params["q"][0]
    if q.find("largest") != -1:
        # Which of the numbers is largest: 841, 973, 279, 146, 923
        return largest_number(q)
    elif q.find(" + ") != -1:
        return sum(q)
    return NAME

if __name__ == "__main__":
    NAME=sys.argv[2]
    MyServer.serve_forever(int(sys.argv[1]))
    print sum("How much is 2 + 11")
    print largest_number("Which of the numbers is largest: 841, 973, 279, 146, 923")
