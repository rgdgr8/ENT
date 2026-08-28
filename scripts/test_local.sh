#!/bin/bash
# LOCAL TEST SCRIPT for MetadataService
#
# Run from ./scripts/:  bash test_local.sh
#
# This script runs unit tests without eso.local/eso.fw dependencies

set -e

echo "============================================"
echo "MetadataService LOCAL UNIT TESTS"
echo "============================================"

# Set environment variables for local build
export GN_LICENSE_VARIANT=NONE
export USE_LOCAL_LIBS=true

cd "$(dirname "$0")/../metadataservice"

echo ""
echo "Running devRowDebug unit tests..."
echo "============================================"

./gradlew testDevRowDebugUnitTest --stacktrace

echo ""
echo "============================================"
echo "Tests completed!"
echo "Test reports: app/build/reports/tests/testDevRowDebugUnitTest/"
echo "============================================"
