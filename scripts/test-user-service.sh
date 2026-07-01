#!/bin/bash

# Get the directory where the script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# Project root is one level up
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Base URL
BASE_URL="localhost:8080"

# Cookies file in scripts directory
COOKIES_FILE="$SCRIPT_DIR/cookies.txt"

# Function to extract JSON value
extract_json() {
    echo "$1" | jq -r "$2" 2>/dev/null
}

# Function to check HTTP status
check_status() {
    local expected=$1
    local actual=$2
    local message=$3
    
    if [ "$expected" -eq "$actual" ]; then
        echo -e "${GREEN}✓ $message${NC}"
    else
        echo -e "${RED}✗ $message (Expected: $expected, Got: $actual)${NC}"
    fi
}

# Function to print section header
print_section() {
    echo -e "\n${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}"
}

# Check if services are running
echo -e "${YELLOW}Checking if services are running...${NC}"
if ! curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/actuator/health" | grep -q "200"; then
    echo -e "${RED}Error: Services not reachable at $BASE_URL${NC}"
    echo -e "${YELLOW}Make sure to run: docker compose up -d${NC}"
    exit 1
fi
echo -e "${GREEN}Services are running!${NC}"

# =============================================
# 1. Registration Tests
# =============================================
print_section "REGISTRATION TESTS"

echo -e "\n${YELLOW}1. Registering test1...${NC}"
REGISTER1_RESPONSE=$(curl -s -w "\n%{http_code}" \
    -X POST "$BASE_URL/auth/register" \
    -d '{"username": "test1", "email": "test1@test.test", "password": "test1234"}' \
    -H "Content-Type: application/json")

REGISTER1_BODY=$(echo "$REGISTER1_RESPONSE" | sed '$d')
REGISTER1_CODE=$(echo "$REGISTER1_RESPONSE" | tail -n1)

check_status 200 "$REGISTER1_CODE" "Register test1"
echo "$REGISTER1_BODY" | jq 2>/dev/null || echo "$REGISTER1_BODY"

# Extract test1 ID
TEST1_ID=$(extract_json "$REGISTER1_BODY" '.id')
if [ -n "$TEST1_ID" ]; then
    echo -e "TEST1_ID: ${GREEN}$TEST1_ID${NC}"
else
    echo -e "${YELLOW}Could not extract TEST1_ID (user might already exist)${NC}"
fi

# =============================================
# 2. Login Tests
# =============================================
print_section "LOGIN TESTS"

echo -e "\n${YELLOW}2. Logging in test1...${NC}"
LOGIN_RESPONSE=$(curl -s -w "\n%{http_code}" \
    -X POST "$BASE_URL/auth/login" \
    -d '{"username": "test1", "email": "test1@test.test", "password": "test1234"}' \
    -H "Content-Type: application/json" \
    -c "$COOKIES_FILE")

LOGIN_BODY=$(echo "$LOGIN_RESPONSE" | sed '$d')
LOGIN_CODE=$(echo "$LOGIN_RESPONSE" | tail -n1)

check_status 200 "$LOGIN_CODE" "Login test1"
echo "$LOGIN_BODY" | jq 2>/dev/null || echo "$LOGIN_BODY"

# =============================================
# 3. Current User Test
# =============================================
print_section "CURRENT USER TEST"

echo -e "\n${YELLOW}3. Getting current user...${NC}"
ME_RESPONSE=$(curl -s -w "\n%{http_code}" \
    -X GET "$BASE_URL/user/me" \
    -b "$COOKIES_FILE")

ME_BODY=$(echo "$ME_RESPONSE" | sed '$d')
ME_CODE=$(echo "$ME_RESPONSE" | tail -n1)

check_status 200 "$ME_CODE" "Get current user"
echo "$ME_BODY" | jq 2>/dev/null || echo "$ME_BODY"

# =============================================
# 4. Conflict Tests
# =============================================
print_section "CONFLICT TESTS"

echo -e "\n${YELLOW}4. Register with duplicate email...${NC}"
CONFLICT1_RESPONSE=$(curl -s -w "\n%{http_code}" \
    -X POST "$BASE_URL/auth/register" \
    -d '{"username": "test1", "email": "test1@test.test", "password": "test1234"}' \
    -H "Content-Type: application/json")

CONFLICT1_CODE=$(echo "$CONFLICT1_RESPONSE" | tail -n1)
check_status 409 "$CONFLICT1_CODE" "Duplicate email returns 409"

echo -e "\n${YELLOW}5. Register with duplicate username (different email)...${NC}"
CONFLICT2_RESPONSE=$(curl -s -w "\n%{http_code}" \
    -X POST "$BASE_URL/auth/register" \
    -d '{"username": "test1", "email": "test2@test.test", "password": "test1234"}' \
    -H "Content-Type: application/json")

CONFLICT2_CODE=$(echo "$CONFLICT2_RESPONSE" | tail -n1)
check_status 409 "$CONFLICT2_CODE" "Duplicate username returns 409"

echo -e "\n${YELLOW}6. Register with same email different username...${NC}"
CONFLICT3_RESPONSE=$(curl -s -w "\n%{http_code}" \
    -X POST "$BASE_URL/auth/register" \
    -d '{"username": "test2", "email": "test1@test.test", "password": "test1234"}' \
    -H "Content-Type: application/json")

CONFLICT3_CODE=$(echo "$CONFLICT3_RESPONSE" | tail -n1)
check_status 409 "$CONFLICT3_CODE" "Duplicate email returns 409"

# =============================================
# 5. Register Test User 2
# =============================================
print_section "REGISTER TEST USER 2"

echo -e "\n${YELLOW}7. Registering test2...${NC}"
REGISTER2_RESPONSE=$(curl -s -w "\n%{http_code}" \
    -X POST "$BASE_URL/auth/register" \
    -d '{"username": "test2", "email": "test2@test.test", "password": "test1234"}' \
    -H "Content-Type: application/json")

REGISTER2_BODY=$(echo "$REGISTER2_RESPONSE" | sed '$d')
REGISTER2_CODE=$(echo "$REGISTER2_RESPONSE" | tail -n1)

check_status 200 "$REGISTER2_CODE" "Register test2"
echo "$REGISTER2_BODY" | jq 2>/dev/null || echo "$REGISTER2_BODY"

# Extract test2 ID
TEST2_ID=$(extract_json "$REGISTER2_BODY" '.id')
if [ -n "$TEST2_ID" ]; then
    echo -e "TEST2_ID: ${GREEN}$TEST2_ID${NC}"
else
    echo -e "${YELLOW}Could not extract TEST2_ID (user might already exist)${NC}"
fi

# =============================================
# 6. Subscription Tests
# =============================================
if [ -n "$TEST1_ID" ] && [ -n "$TEST2_ID" ]; then
    print_section "SUBSCRIPTION TESTS"
    
    echo -e "\n${YELLOW}8. Get test2 subscribers (should be empty)...${NC}"
    SUBS_BEFORE=$(curl -s -w "\n%{http_code}" \
        -X GET "$BASE_URL/user/$TEST2_ID/subscribers" \
        -b "$COOKIES_FILE")
    
    SUBS_BEFORE_BODY=$(echo "$SUBS_BEFORE" | sed '$d')
    SUBS_BEFORE_CODE=$(echo "$SUBS_BEFORE" | tail -n1)
    
    check_status 200 "$SUBS_BEFORE_CODE" "Get subscribers (empty)"
    echo "$SUBS_BEFORE_BODY" | jq 2>/dev/null || echo "$SUBS_BEFORE_BODY"
    
    echo -e "\n${YELLOW}9. Subscribe test1 to test2...${NC}"
    SUB_RESPONSE=$(curl -s -w "\n%{http_code}" \
        -X POST "$BASE_URL/user/subscribe/$TEST2_ID" \
        -b "$COOKIES_FILE")
    
    SUB_CODE=$(echo "$SUB_RESPONSE" | tail -n1)
    check_status 200 "$SUB_CODE" "Subscribe to test2"
    
    echo -e "\n${YELLOW}10. Get test2 subscribers again...${NC}"
    SUBS_AFTER=$(curl -s -w "\n%{http_code}" \
        -X GET "$BASE_URL/user/$TEST2_ID/subscribers" \
        -b "$COOKIES_FILE")
    
    SUBS_AFTER_BODY=$(echo "$SUBS_AFTER" | sed '$d')
    SUBS_AFTER_CODE=$(echo "$SUBS_AFTER" | tail -n1)
    
    check_status 200 "$SUBS_AFTER_CODE" "Get subscribers (with test1)"
    echo "$SUBS_AFTER_BODY" | jq 2>/dev/null || echo "$SUBS_AFTER_BODY"
    
    echo -e "\n${YELLOW}11. Get test1 subscriptions...${NC}"
    SUBSCRIPTIONS=$(curl -s -w "\n%{http_code}" \
        -X GET "$BASE_URL/user/$TEST1_ID/subscriptions" \
        -b "$COOKIES_FILE")
    
    SUBSCRIPTIONS_BODY=$(echo "$SUBSCRIPTIONS" | sed '$d')
    SUBSCRIPTIONS_CODE=$(echo "$SUBSCRIPTIONS" | tail -n1)
    
    check_status 200 "$SUBSCRIPTIONS_CODE" "Get subscriptions"
    echo "$SUBSCRIPTIONS_BODY" | jq 2>/dev/null || echo "$SUBSCRIPTIONS_BODY"
else
    echo -e "\n${YELLOW}Skipping subscription tests - user IDs not found${NC}"
    echo "Make sure test1 and test2 are registered successfully first"
fi

# =============================================
# Summary
# =============================================
print_section "TEST SUMMARY"

echo -e "TEST1_ID: ${GREEN}${TEST1_ID:-Not found}${NC}"
echo -e "TEST2_ID: ${GREEN}${TEST2_ID:-Not found}${NC}"
echo -e "Cookies saved: ${GREEN}$COOKIES_FILE${NC}"
echo ""

echo -e "${YELLOW}Manual commands:${NC}"
echo -e "Login as test1:"
echo -e "  ${GREEN}curl -X POST $BASE_URL/auth/login -d '{\"email\": \"test1@test.test\", \"password\": \"test1234\"}' -H \"Content-Type: application/json\" -c $COOKIES_FILE${NC}"
echo ""
echo -e "Get current user:"
echo -e "  ${GREEN}curl -X GET $BASE_URL/user/me -b $COOKIES_FILE${NC}"
echo ""

echo -e "${YELLOW}To cleanup test data, restart the services:${NC}"
echo -e "  ${GREEN}docker compose restart${NC}"

print_section "TESTS COMPLETE"
