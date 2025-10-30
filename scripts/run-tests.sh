#!/bin/bash

# Script to run tests with different options

set -e

echo "========================================="
echo "Kafka AsyncAPI - Test Runner"
echo "========================================="
echo ""

# Function to display usage
usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  all           Run all tests (default)"
    echo "  unit          Run unit tests only"
    echo "  cucumber      Run Cucumber BDD tests only"
    echo "  coverage      Run tests with coverage report"
    echo "  debug         Run tests with debug logging"
    echo ""
    echo "Examples:"
    echo "  $0 all"
    echo "  $0 cucumber"
    echo "  $0 coverage"
    exit 1
}

# Default to all tests
TEST_TYPE="${1:-all}"

case $TEST_TYPE in
    all)
        echo "🧪 Running all tests..."
        mvn clean test
        ;;
    
    unit)
        echo "🧪 Running unit tests only..."
        mvn test -Dtest=*Test
        ;;
    
    cucumber)
        echo "🥒 Running Cucumber BDD tests only..."
        mvn test -Dtest=CucumberTestRunner
        ;;
    
    coverage)
        echo "📊 Running tests with coverage report..."
        mvn clean test jacoco:report
        echo ""
        echo "Coverage report generated at: target/site/jacoco/index.html"
        echo "Opening report..."
        if [[ "$OSTYPE" == "darwin"* ]]; then
            open target/site/jacoco/index.html
        elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
            xdg-open target/site/jacoco/index.html
        fi
        ;;
    
    debug)
        echo "🐛 Running tests with debug logging..."
        mvn test -X -Dlogging.level.com.asyncapi=DEBUG
        ;;
    
    -h|--help|help)
        usage
        ;;
    
    *)
        echo "❌ Unknown option: $TEST_TYPE"
        echo ""
        usage
        ;;
esac

echo ""
echo "========================================="
echo "📊 Test Results Summary"
echo "========================================="
echo ""
echo "View detailed reports:"
echo "  • HTML Report: target/cucumber-reports/cucumber.html"
echo "  • JSON Report: target/cucumber-reports/cucumber.json"
echo "  • Surefire Reports: target/surefire-reports/"
echo ""

if [ -f "target/cucumber-reports/cucumber.html" ]; then
    echo "Opening Cucumber report..."
    if [[ "$OSTYPE" == "darwin"* ]]; then
        open target/cucumber-reports/cucumber.html
    elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
        xdg-open target/cucumber-reports/cucumber.html
    fi
fi

