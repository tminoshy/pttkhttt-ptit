#!/bin/sh
set -eu

ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
BUILD="$ROOT/build"
STAGE="$BUILD/src"
PANDOC=${PANDOC:-pandoc}

if [ -d "$BUILD" ]; then
  rm -rf "$BUILD"
fi
mkdir -p "$STAGE"

for source in question.md spec.md analysis.md desing.md implement.md; do
  if [ "$source" = question.md ]; then
    cp "$ROOT/$source" "$STAGE/$source"
  else
    cp "$ROOT/report/$source" "$STAGE/$source"
  fi
  perl -pi -e 's/\.svg([)#])/.png$1/g; s/\.svg\b/.png/g' "$STAGE/$source"
  perl -pi -e 's{\]\((<?)\.\./}{]($1}g' "$STAGE/$source"
done

"$PANDOC" "$STAGE/spec.md" \
  -o "$BUILD/spec.docx" --resource-path="$ROOT:$STAGE" --toc
"$PANDOC" "$STAGE/analysis.md" \
  -o "$BUILD/analysis.docx" --resource-path="$ROOT:$STAGE" --toc
"$PANDOC" \
  "$STAGE/question.md" "$STAGE/spec.md" "$STAGE/analysis.md" \
  "$STAGE/desing.md" "$STAGE/implement.md" \
  -o "$BUILD/24B23DCCE064.docx" --resource-path="$ROOT:$STAGE" --toc

printf '%s\n' \
  "$BUILD/spec.docx" \
  "$BUILD/analysis.docx" \
  "$BUILD/24B23DCCE064.docx"
