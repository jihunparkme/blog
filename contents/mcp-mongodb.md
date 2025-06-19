# MongoDB MCP Server

지난 시간에 다룬 [5분만에 GitHub MCP Server 구축해서 GitHub 작업 시키기](https://data-make.tistory.com/799)에 이어 MongoDB MCP Server도 구축해 보려고 합니다.

## MongoDB MCP Server

[mongodb-mcp-server](https://github.com/mongodb-developer/mongodb-mcp-server)도 지난 GitHub MCP Server와 동일하게 `mcp.servers` 목록에 `mongodb`만 추가해 주면 준비는 끝입니다.


```json
{
    "mcp": {
        "inputs": [],
        "servers": {
            "mongodb": {
                "command": "npx",
                "args": [
                    "-y", 
                    "mongodb-mcp-server", 
                    "--connectionString", 
                    "mongodb+srv://<yourcluster>",
                    "--readOnly"
                ],
            }
        }
    },
}
```
