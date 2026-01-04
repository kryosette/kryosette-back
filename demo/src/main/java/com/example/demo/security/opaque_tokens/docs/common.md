curl -X POST http://localhost:8088/api/v1/tokens/create \
-H "Content-Type: application/json" \
-H "Authorization: Bearer db26103a-10af-4160-b616-e663a1f56b16" \
-d '{
"userId": "user123",
"userData": "{\"roles\":[\"USER\"],\"email\":\"user@example.com\"}",
"ttlSeconds": 3600
}'