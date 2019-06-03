#!/bin/bash

BRANCH=${GIT_BRANCH##origin/}

echo "Committing to branch: $BRANCH"

git pull --no-edit origin $BRANCH:$BRANCH
git add -A src/
git add -A gen-rb/
git commit -m "Commit auto-generated source"
git push origin $BRANCH:$BRANCH