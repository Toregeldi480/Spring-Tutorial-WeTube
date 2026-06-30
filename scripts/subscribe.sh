curl -X POST localhost:8080/auth/register -H "Content-Type: application/json" -d '{"username": "test1", "email": "test1@test.test", "password": "test1234"}' &&
curl -X POST localhost:8080/auth/register -H "Content-Type: application/json" -d '{"username": "test2", "email": "test2@test.test", "password": "test1234"}' &&
test1_id=$( curl -X POST localhost:8080/auth/login    -H "Content-Type: application/json" -d '{"username": "test1", "password": "test1234}' -c cookies1.txt | jq --raw-output '.id' ) &&
curl -X POST localhost:8080/auth/login    -H "Content-Type: application/json" -d '{"email": "test2", "password": "test1234}' -c cookies2.txt &&
curl -X POST localhost:8080/user/subscribe/$test1_id -b cookies2.txt | jq
