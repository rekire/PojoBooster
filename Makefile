all: publish app

clean:
	./gradlew cle -q

publish:
	./gradlew -Pfrom=scratch clean publishToMavenLocal -q

app:
	./gradlew clean :ex:app:aDeb :ex:lib:aDeb :ex:java:jar -s
