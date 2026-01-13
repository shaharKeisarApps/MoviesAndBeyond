#!/bin/bash
# Find all Coil usages in the codebase
# This script helps identify files that need migration to Landscapist

echo "=== Coil Usage Report ==="
echo ""

echo "### Coil Imports ###"
grep -rn "import coil" --include="*.kt" . 2>/dev/null || echo "No Coil imports found"
echo ""

echo "### AsyncImage Usage ###"
grep -rn "AsyncImage" --include="*.kt" . 2>/dev/null || echo "No AsyncImage usage found"
echo ""

echo "### SubcomposeAsyncImage Usage ###"
grep -rn "SubcomposeAsyncImage" --include="*.kt" . 2>/dev/null || echo "No SubcomposeAsyncImage usage found"
echo ""

echo "### rememberAsyncImagePainter Usage ###"
grep -rn "rememberAsyncImagePainter" --include="*.kt" . 2>/dev/null || echo "No rememberAsyncImagePainter usage found"
echo ""

echo "### ImageLoader Usage ###"
grep -rn "ImageLoader" --include="*.kt" . 2>/dev/null || echo "No ImageLoader usage found"
echo ""

echo "### ImageLoaderFactory Usage ###"
grep -rn "ImageLoaderFactory" --include="*.kt" . 2>/dev/null || echo "No ImageLoaderFactory usage found"
echo ""

echo "### Coil Dependencies in build.gradle ###"
grep -rn "coil" --include="*.gradle.kts" --include="*.toml" . 2>/dev/null || echo "No Coil dependencies found"
echo ""

echo "=== Summary ==="
TOTAL=$(grep -r "coil\|AsyncImage\|SubcomposeAsyncImage" --include="*.kt" . 2>/dev/null | wc -l)
echo "Total Coil-related occurrences: $TOTAL"
