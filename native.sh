rm -rf target
echo "building native image.."
./mvnw -Pnative -DskipTests native:compile && ./target/demo
