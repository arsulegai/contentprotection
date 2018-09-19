How to build this project?
______________________________
1) Generate the protobuf files from proto folder of sawtooth-sdk-go and save in GOPATH
2) To auto generate, get the SDK using -> go get github.com/hyperledger/sawtooth-sdk-go, this gets saved in your first GOPATH location. Then run "go generate" from this folder.
3) Run "go build cp-client.go"

How to run?
______________________________
1) Binary expects system environment variable VALIDATOR_URL be set to the location of REST call, ex: http://localhost:8008
2) cp-client -user=<username> -type=<USER|CONTENT> -action=<REGISTER|CREATE|UPDATE|READ> -role=<NONE|LEVEL1|LEVEL2|ADMIN> -id=<id> -content=<content>