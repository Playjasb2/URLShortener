# URLShortener

As part of a school project, my group was instructed to create a URL Shortner that is said to be scalable.

We designed the system by initially using 3 machines. Each of these machines will run a web server to host our site to shorten URLs. Also, one of these machines will also host a proxy server.

All the user traffic is handled through the proxy server. When the user goes to the site for the first time, they'll send a request to the proxy server, and the proxy server will respond by giving them webpages. If the user starts sending GET or PUT requests, the proxy server will instantiate a new thread to handle the request, and have that specific thread relay the request to one of the webservers. The webserver will instantiate a new thread to handle the request, and send a response back to the proxy server. The thread on the proxy server will then relay the response back to the user, and terminate.

As for how the proxy server decides which webserver to relay the request to, the user's request is hashed, and we determine the webserver by the range of values that the hash will be in. Each of the web servers will make use of some custom database solution using text files, in an attempt to speed up reading and writing data using bytes.

Also, we have a thread in each web server that would send all of the new user data saved for the past hour to the next webserver. Also, we have each the web servers use a concurrent hashmap to serve as a cache to quickly respond to user's GET requests, instead of having to read from our database, which would be slower.

Each of the web servers are also programmed to have an additional thread respond to messages from a monitor, executed from `Monitor.py`. The monitor can send periodic ping messages to each of the servers, and the servers would respond to the messages to indicate to the monitor to indicate that it's still alive. The Monitor would display an interface in the terminal of all the servers and their status. Should one go offline, the monitor presents an option to the user to kill the malfunctioning server if it continues to run, instantiate a new URL Shortener instance, and tell the server that has its backup to start healing the needed server, by feeding it all the user data it needs to be fully functional as before. The monitor would also send messages to the proxy server to do a redirect for any new user requests that occur during this time, and then undo the redirect when it's finished.
