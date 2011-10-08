import sys
from BaseHTTPServer import BaseHTTPRequestHandler,HTTPServer

class MyServer(BaseHTTPRequestHandler):
    def do_GET(self):
        self.send_response(200, 'OK')
        self.send_header('Content-type', 'text/html')
        self.end_headers()
        self.wfile.write( NAME )

    @staticmethod
    def serve_forever(port):
        HTTPServer(('', port), MyServer).serve_forever()

if __name__ == "__main__":
    NAME=sys.argv[2]
    MyServer.serve_forever(int(sys.argv[1]))
