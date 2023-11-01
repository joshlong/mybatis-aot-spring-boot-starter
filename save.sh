rm -rf target
echo "building native image.."
./mvnw spring-javaformat:apply && git commit -am polish && git push
