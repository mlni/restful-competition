import sys, cgi, re, operator, random, fractions, time
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

        if 0 and random.random() < 0.1:
            self.send_response(200, 'OK')
            self.send_header('Content-type', 'text/plain')
            self.end_headers()
            self.wfile.write(NAME)
            return
        
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
                ctype = self.headers.getheader("accept")
                self.send_response(200, 'OK')
                self.send_header('Content-type', ctype)
                self.end_headers()

                if self.headers.get("Range"):
                    range_h = self.headers.get("Range")
                    print "Range header: %s" % range_h 
                    st, end = re.search("bytes=([0-9]+)-([0-9]+)", range_h).groups()
                    st, end = int(st), int(end)
                    
                    self.wfile.write( REST_STATE[self.path][ctype][st:end] )
                else:
                    self.wfile.write( REST_STATE[self.path][ctype] )
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
        ctype = self.headers.getheader('content-type')
        print "Got", ctype, data, self.path
        
        content = REST_STATE.get(self.path, {})
        content[ctype] = data
        REST_STATE[self.path] = content
        
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

def arithm_params(q, params, cookies):
    print "arithm params"
    ops = { "+": operator.add, "-": operator.sub, "*": operator.mul }
    m = re.search("is ([a-z]+) ([\+*/-]) ([a-z]+)", q)
    print cookies
    print params
    
    p1 = int(m.group(1) in params and params[m.group(1)][0] or cookies[m.group(1)])
    p2 = int(m.group(3) in params and params[m.group(3)][0] or cookies[m.group(3)])
    return ops[m.group(2)](p1, p2)

def arithm(q):
    print "arithm plain"
    ops = { "+": operator.add, "-": operator.sub, "*": operator.mul, "/": operator.div }
    m = re.search("is ([0-9]+) ([\+*/-]) ([0-9]+)", q)
    return ops[m.group(2)](int(m.group(1)), int(m.group(3)))

def arithm_hex(q):
    ops = { "+": operator.add, "-": operator.sub, "*": operator.mul, "/": operator.div }
    m = re.search("is ([0-9a-z]+) ([\+*/-]) ([0-9a-z]+)", q)
    x = int(m.group(1), 16)
    y = int(m.group(3), 16)
    r = ops[m.group(2)](x, y)
    print "%s %s %s = %s (%s)" % (x, m.group(2), y, r, hex(r))
    return hex(r)

def factorial(q):
    n = re.search("What is the factorial of ([0-9]+)", q).group(1)
    n = int(n)
    r = 1
    for i in range(1, n+1):
        r *= i
    print "Factorial on %s = %s" % (n, r)
    return r

def gcd(q):
    m = re.search("What is the greatest common divisor of ([0-9]+) and ([0-9]+)", q)
    x = int(m.group(1))
    y = int(m.group(2))
    return fractions.gcd(x, y)

def days_between(q):
    m = re.search("How many days are between ([0-9\.-]+) and ([0-9\.-]+)", q)
    d1 = parse_date(m.group(1))
    d2 = parse_date(m.group(2))
    r = abs(int(round(d1 - d2) / (24 * 60 * 60)))
    print "Days between %s and %s = %s" % (d1, d2, r)
    return r

def parse_date(d):
    m = re.search("([0-9]{2}\.[0-9]{2}\.[0-9]{4})", d)
    if m:
        return time.mktime(time.strptime(m.group(1), "%d.%m.%Y"))
    else:
        m = re.search("([0-9-]+)", d)
        return time.mktime(time.strptime(m.group(1), "%Y-%m-%d"))

def weekday(q):
    m = re.search("What was the weekday of ([0-9\.-]+)", q)
    d = parse_date(m.group(1))
    d = time.localtime(d)
    return ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"][d[6]]

def earliest(q):
    days = q[q.index(":")+1:].split(", ")
    ds = {}
    for d in days:
        ds[parse_date(d.strip())] = d
    r = sorted(ds.keys())[0]
    print "earliest: %s" % (ds[r])
    return ds[r]

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

    cookies = {}
    if "Cookie" in headers:
        pcs = headers["Cookie"].split("; ")
        for p in pcs:
            k, v = p.split("=")
            cookies[k.strip()] = v.strip()

    q = params["q"][0]

    if "X-The-Ultimate-Question" in headers:
        print "Ultimate question"
        result = "42"
    elif q.find("largest") != -1:
        # Which of the numbers is largest: 841, 973, 279, 146, 923
        result = largest_number(q)
    elif re.search("How much is [a-z] [\+*/-] [a-z]", q):
        result = arithm_params(q, params, cookies)
    elif re.search("How much is [0-9]+ [\+*/-] [0-9]+", q):
        result = arithm(q)
    elif re.search("How much is 0x[0-9a-h]+ [\+*/-] 0x[0-9a-h]+", q):
        result = arithm_hex(q)
    elif re.search("What is the factorial of [0-9]+", q):
        result = factorial(q)
    elif re.search("What is the greatest common divisor of .*", q):
        result = gcd(q)
    elif q.find("Which page am I coming from") != -1:
        result = referer(headers)
    elif q.find("What was the weekday of ") != -1:
        result = weekday(q)
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
    elif re.search("\w+ is [0-9'\"]+ tall. How tall is he in centimeters", q):
        name = q.split()[0]

        m = re.search("([0-9])'", q)
        feet = int(m.group(1))
        inches = 0
        m = re.search('([0-9]+)"', q)
        if m:
            inches = int(m.group(1))
        result = int(round(feet * 12 * 2.54 + inches * 2.54))
        vals = {result : name}
        s = sid()
        SESSIONS[s] = vals
        out.append(["Set-Cookie", "SID=%s; domain=localhost" % s])
        print "%s'%s = %s" % (feet, inches, result)
    elif re.search("\w+ is [0-9]+ cm tall. How tall is he in inches", q):
        s = sessioncookie(headers)
        sess = SESSIONS[s]

        name = q.split()[0]
        m = re.search("([0-9]+) cm", q)
        cm = int(m.group(1))
        result = int(round(cm / 2.54))
        sess[cm] = name
        SESSIONS[s] = sess
        print "%s cm = %s inches" % (cm, result)
    elif q.find("Which of them is taller") != -1:
        s = sessioncookie(headers)
        vals = SESSIONS[s]
        taller = sorted(vals.keys())[-1]
        result = vals[taller]
        print "Taller: %s" % result
        
    elif q.find("Fibonacci") != -1:
        n = re.search(" ([0-9]+)th number in Fibonacci", q).group(1)
        result = fib(int(n))
        print "fibonacci: %s = %s" % (n, result)
    elif q.find("Which of the following days is the earliest") != -1:
        result = earliest(q)
    elif q.find("How many days are between") != -1:
        result = days_between(q)
    elif q.find("What is my user agent") != -1:
        result = headers.get("User-Agent")
    elif q.find("Which browser am I using") != -1:
        result = headers.get("User-Agent")
    else:
        print "No match!"
        
    return (out, result)

def fib(n):
    a, b = 0, 1
    for i in range(n - 1):
        c = a + b
        a, b = b, c
    return c

if __name__ == "__main__":
    NAME=sys.argv[2]
    SESSIONS={}
    MyServer.serve_forever(int(sys.argv[1]))
    print largest_number("Which of the numbers is largest: 841, 973, 279, 146, 923")
