rm -rf ./outputdir/
find . -type f -name "*.java" | xargs javadoc -d outputdir -private