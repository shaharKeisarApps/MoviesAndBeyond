#!/bin/bash
# Check the latest Landscapist version from Maven Central
# This script helps ensure you're using the latest version

echo "=== Landscapist Version Check ==="
echo ""

# Check current version in project
echo "### Current Project Version ###"
if [ -f "gradle/libs.versions.toml" ]; then
    grep -i "landscapist" gradle/libs.versions.toml 2>/dev/null || echo "Landscapist not configured in project"
else
    echo "libs.versions.toml not found"
fi
echo ""

# Fetch latest version from Maven Central
echo "### Latest Version (Maven Central) ###"
LATEST=$(curl -s "https://search.maven.org/solrsearch/select?q=g:com.github.skydoves+AND+a:landscapist-core&rows=1&wt=json" 2>/dev/null | grep -o '"latestVersion":"[^"]*"' | cut -d'"' -f4)

if [ -n "$LATEST" ]; then
    echo "Latest landscapist version: $LATEST"
else
    echo "Could not fetch latest version. Check manually at:"
    echo "https://github.com/skydoves/landscapist/releases"
fi
echo ""

echo "### Available Modules ###"
echo "Core modules:"
echo "  - landscapist-core (KMP image loading engine)"
echo "  - landscapist-image (LandscapistImage composable)"
echo ""
echo "Plugin modules:"
echo "  - landscapist-animation (CrossfadePlugin, CircularRevealPlugin)"
echo "  - landscapist-placeholder (ShimmerPlugin, PlaceholderPlugin, ThumbnailPlugin)"
echo "  - landscapist-transformation (BlurTransformationPlugin)"
echo "  - landscapist-palette (PalettePlugin)"
echo "  - landscapist-zoomable (ZoomablePlugin)"
echo ""

echo "### Recommended Dependencies ###"
cat << 'EOF'
# Add to libs.versions.toml:
[versions]
landscapist = "2.4.7"  # Update to latest

[libraries]
# Core (required)
landscapist-core = { module = "com.github.skydoves:landscapist-core", version.ref = "landscapist" }
landscapist-image = { module = "com.github.skydoves:landscapist-image", version.ref = "landscapist" }

# Plugins (optional)
landscapist-animation = { module = "com.github.skydoves:landscapist-animation", version.ref = "landscapist" }
landscapist-placeholder = { module = "com.github.skydoves:landscapist-placeholder", version.ref = "landscapist" }
landscapist-palette = { module = "com.github.skydoves:landscapist-palette", version.ref = "landscapist" }
landscapist-transformation = { module = "com.github.skydoves:landscapist-transformation", version.ref = "landscapist" }
landscapist-zoomable = { module = "com.github.skydoves:landscapist-zoomable", version.ref = "landscapist" }

# In build.gradle.kts:
implementation(libs.landscapist.core)
implementation(libs.landscapist.image)
implementation(libs.landscapist.animation)
implementation(libs.landscapist.placeholder)
EOF
