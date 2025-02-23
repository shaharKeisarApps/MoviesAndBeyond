#!/bin/bash

# Exit immediately if a command exits with a non-zero status
set -e

# Variables for the old and new project name and package name
OLD_PROJECT_NAME="MovieInfo"
NEW_PROJECT_NAME="MoviesAndBeyond"

OLD_PACKAGE_NAME="com.anshtya.movieinfo"
NEW_PACKAGE_NAME="com.keisardev.moviesandbeyond"

# Convert package names to directory paths
OLD_PACKAGE_DIR="${OLD_PACKAGE_NAME//./\/}"
NEW_PACKAGE_DIR="${NEW_PACKAGE_NAME//./\/}"

# Update the project name in all settings.gradle or settings.gradle.kts files
find . -name "settings.gradle" -o -name "settings.gradle.kts" | while read -r file; do
  sed -i "" "s/rootProject\.name = \"$OLD_PROJECT_NAME\"/rootProject.name = \"$NEW_PROJECT_NAME\"/g" "$file"
  echo "Updated project name in $file."
done

# Update the package name in the project
find . -type f \( -name "*.java" -o -name "*.kt" -o -name "*.xml" -o -name "AndroidManifest.xml" \) -exec sed -i "" "s/$OLD_PACKAGE_NAME/$NEW_PACKAGE_NAME/g" {} +

# Update namespace in gradle files
find . -type f -name "build.gradle" -o -name "build.gradle.kts" | while read -r file; do
  sed -i "" "s/namespace = \"$OLD_PACKAGE_NAME\"/namespace = \"$NEW_PACKAGE_NAME\"/g" "$file"
  echo "Updated namespace in $file."
done

# Rename class and function names containing old project name
find . -type f \( -name "*.java" -o -name "*.kt" -o -name "*.xml" \) | while read -r file; do
  sed -i "" "s/$OLD_PROJECT_NAME/$NEW_PROJECT_NAME/g" "$file"
  echo "Updated occurrences of $OLD_PROJECT_NAME in $file."
done

# Move all files to the updated package and delete old package directories
find . -type d -path "*/src/*/java/$OLD_PACKAGE_DIR" -o -path "*/src/*/kotlin/$OLD_PACKAGE_DIR" | while read -r dir; do
  NEW_DIR=$(echo "$dir" | sed "s|$OLD_PACKAGE_DIR|$NEW_PACKAGE_DIR|")
  mkdir -p "$NEW_DIR"
  find "$dir" -type f -exec mv {} "$NEW_DIR/" \;
  if [ -z "$(ls -A "$dir" 2>/dev/null)" ]; then
    rmdir "$dir"
    echo "Moved files and deleted old directory $dir."
  fi
done

# Update theme references
find . -type f -name "*.xml" | while read -r file; do
  sed -i "" "s/Theme\.$OLD_PROJECT_NAME/Theme.$NEW_PROJECT_NAME/g" "$file"
  echo "Updated theme references in $file."
done

# Update the build.gradle or build.gradle.kts applicationId if present
find . -name "build.gradle" -o -name "build.gradle.kts" | while read -r file; do
  sed -i "" "s/applicationId \"$OLD_PACKAGE_NAME\"/applicationId \"$NEW_PACKAGE_NAME\"/g" "$file"
  echo "Updated applicationId in $file."
done

# Clean up any empty directories left behind
find . -type d -empty -delete

echo "Script re-run complete. Any necessary changes have been applied without duplications."

echo "Project and package name update completed successfully."
