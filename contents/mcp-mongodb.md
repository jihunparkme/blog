# MongoDB MCP Server

## 


```json
{
    "inputs": [
        // ...
    ],
    "servers": {
      "mongodb": {
        "command": "docker",
        "args": [
          "run",
          "-i",
          "--rm",
          "-e",
          "MONGODB_URI",
          "-p",
          "27017:27017",
          "mongodb/mongodb-mcp-server"
        ],
        "env": {
          "MONGODB_URI": "${input:mongodb_uri}"
        }
      }
    }
}
```

https://www.mongodb.com/blog/post/announcing-mongodb-mcp-server

https://www.mongodb.com/ko-kr/docs/manual/mcp/