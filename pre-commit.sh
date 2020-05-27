#!/bin/bash
#
# environmental args expected:
# BUMP_MAJOR_VERSION, BUMP_MINOR_VERSION, VERSION_COMMENT
# if either of the bumps are true, then increases that version, resets the other (lesser) versions
# afterwards, removes those flags
# VERSION_COMMENT can be any comment which will be added to the version data file
# for example: a projectname -- robin, bluebird, apex, etc; VERSION_COMMENT will persist
# adds env variable: GIT_VERSION_TAG
#
#versionfile="VersionData.kt"
versionfile="temp.txt"
filepath="$(pwd)/src/$versionfile"

version_regex='v([0-9]+)\.([0-9]+)\.([0-9]+)'
git_string=$(git describe --tags)

if [[ $git_string =~ $version_regex ]]; then
    major_version="${BASH_REMATCH[1]}"
    minor_version="${BASH_REMATCH[2]}"
    patch_version="${BASH_REMATCH[3]}"
else
    echo "Error: git describe did not output a valid version string. Unable to update $versionfile" >&2
    exit 1
fi

# logic

if [ -n "$BUMP_MAJOR_VERSION" ]
then
   let major_version+=1
   minor_version=1
   patch_version=0
else if [ -n "$BUMP_MINOR_VERSION" ]
  then
    let minor_version+=1
    patch_version=0
  else
    let patch_version+=1
  fi
fi

if [ -z $VERSION_COMMENT ]
then
  version_comment=""
else
  version_comment=$VERSION_COMMENT
fi

#

version_string="v${major_version}.${minor_version}.${patch_version} $version_comment"

echo "updating version number to: $version_string" >&2
export GIT_VERSION_TAG=$version_string
unset BUMP_MAJOR_VERSION BUMP_MINOR_VERSION


# Working directory of a git hook is always the root of the repo
# cat > $filepath <<EOM
cat  <<EOM
package Fatashi
// NOTE: this file is automatically updated by .git/hooks/pre-commit script
// use environmental variables: BUMP_MAJOR_VERSION, BUMP_MINOR_VERSION, VERSION_COMMENT
//     to control logic in pre-commit
// values are ONLY used in version.kt enum Version

const val _MAJOR_VERSION=$major_version
const val _MINOR_VERSION=$minor_version
const val _PATCH_VERSION=$patch_version
const val _VERSION_COMMENT="$version_comment"
}
EOM

# git add $filepath
