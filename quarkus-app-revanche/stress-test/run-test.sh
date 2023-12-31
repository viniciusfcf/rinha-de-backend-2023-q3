# Exemplos de requests
# curl -v -XPOST -H "content-type: application/json" -d '{"apelido" : "xpto", "nome" : "xpto xpto", "nascimento" : "2000-01-01", "stack": null}' "http://localhost:9999/pessoas"
# curl -v -XGET "http://localhost:9999/pessoas/1"
# curl -v -XGET "http://localhost:9999/pessoas?t=xpto"
# curl -v "http://localhost:9999/contagem-pessoas"

#GATLING_BIN_DIR=$HOME/gatling/3.9.5/bin

#sh $GATLING_BIN_DIR/gatling.sh -ro ~/des/rinha-de-backend-2023-q3-public/stress-test/user-files/results/rinhabackendsimulation-20230813020445855

GATLING_BIN_DIR=gatling-3.9.5/bin

WORKSPACE=~/des/rinha-de-backend-2023-q3-public/stress-test

sh $GATLING_BIN_DIR/gatling.sh -rm local -s RinhaBackendSimulation \
    -rd "DESCRICAO" \
    -rf $WORKSPACE/user-files/results \
    -sf $WORKSPACE/user-files/simulations \
    -rsf $WORKSPACE/user-files/resources \

sleep 3

curl -v "http://localhost:9999/contagem-pessoas"