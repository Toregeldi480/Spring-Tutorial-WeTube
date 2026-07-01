cd gateway-service/     && ./mvnw clean install && cd .. &&
cd registry-service/    && ./mvnw clean install && cd .. &&
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
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Service directories (in build order)
SERVICES=(
    "registry-service"
    "gateway-service"
    "user-service"
    "video-service"
    "transcoding-service"
)

# Flags
SKIP_TESTS=true
VERBOSE=false
BUILD_DOCKER=true

# Function to print section header
print_header() {
    echo -e "\n${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║${NC} ${CYAN}$1${NC}"
    echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
}

# Function to print success
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

# Function to print error
print_error() {
    echo -e "${RED}✗ $1${NC}"
}

# Function to print info
print_info() {
    echo -e "${YELLOW}→ $1${NC}"
}

# Function to show spinner
spinner() {
    local pid=$1
    local delay=0.1
    local spinstr='|/-\'
    while ps -p "$pid" > /dev/null 2>&1; do
        local temp=${spinstr#?}
        printf " [%c]  " "$spinstr"
        local spinstr=$temp${spinstr%"$temp"}
        sleep $delay
        printf "\b\b\b\b\b\b"
    done
    printf "    \b\b\b\b"
}

# Parse command line arguments
while [[ "$#" -gt 0 ]]; do
    case $1 in
        --with-tests) SKIP_TESTS=false ;;
        --verbose|-v) VERBOSE=true ;;
        --no-docker) BUILD_DOCKER=false ;;
        --help|-h)
            echo "Usage: $0 [options]"
            echo ""
            echo "Options:"
            echo "  --with-tests    Run tests during build"
            echo "  --verbose, -v   Verbose Maven output"
            echo "  --no-docker     Skip Docker image build"
            echo "  --help, -h      Show this help message"
            exit 0
            ;;
        *) echo "Unknown parameter: $1"; exit 1 ;;
    esac
    shift
done

# Navigate to project root
cd "$PROJECT_ROOT" || exit 1

# =============================================
# Start
# =============================================
clear
echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║${NC}        ${CYAN}WeTube Build Script${NC}                         ${BLUE}║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "Project Root: ${GREEN}$PROJECT_ROOT${NC}"
echo -e "Skip Tests:   ${GREEN}$SKIP_TESTS${NC}"
echo -e "Build Docker: ${GREEN}$BUILD_DOCKER${NC}"
echo -e "Verbose:      ${GREEN}$VERBOSE${NC}"

# =============================================
# Build JARs
# =============================================
print_header "Building JAR Files"

BUILD_START=$(date +%s)
MAVEN_OPTS="clean package"

if [ "$SKIP_TESTS" = true ]; then
    MAVEN_OPTS="$MAVEN_OPTS -DskipTests"
fi

if [ "$VERBOSE" = false ]; then
    MAVEN_OPTS="$MAVEN_OPTS -q"
fi

BUILD_FAILED=false

for i in "${!SERVICES[@]}"; do
    SERVICE="${SERVICES[$i]}"
    COUNT=$((i + 1))
    TOTAL=${#SERVICES[@]}
    
    print_info "[$COUNT/$TOTAL] Building ${CYAN}$SERVICE${NC}..."
    
    cd "$PROJECT_ROOT/$SERVICE" || {
        print_error "Directory not found: $SERVICE"
        BUILD_FAILED=true
        break
    }
    
    START_TIME=$(date +%s)
    
    if $VERBOSE; then
        ./mvnw $MAVEN_OPTS
        EXIT_CODE=$?
    else
        ./mvnw $MAVEN_OPTS &
        PID=$!
        spinner $PID
        wait $PID
        EXIT_CODE=$?
    fi
    
    END_TIME=$(date +%s)
    DURATION=$((END_TIME - START_TIME))
    
    if [ $EXIT_CODE -eq 0 ]; then
        print_success "$SERVICE built successfully (${DURATION}s)"
    else
        print_error "$SERVICE build failed (${DURATION}s)"
        BUILD_FAILED=true
        break
    fi
    
    cd "$PROJECT_ROOT" || exit 1
done

BUILD_END=$(date +%s)
BUILD_DURATION=$((BUILD_END - BUILD_START))

if [ "$BUILD_FAILED" = true ]; then
    print_header "BUILD FAILED"
    echo -e "${RED}One or more services failed to build${NC}"
    exit 1
fi

print_success "All JARs built successfully (${BUILD_DURATION}s)"

# =============================================
# Build Docker Images
# =============================================
if [ "$BUILD_DOCKER" = true ]; then
    print_header "Building Docker Images"
    
    print_info "Running docker compose build..."
    
    DOCKER_START=$(date +%s)
    
    docker compose build
    
    if [ $? -eq 0 ]; then
        DOCKER_END=$(date +%s)
        DOCKER_DURATION=$((DOCKER_END - DOCKER_START))
        print_success "Docker images built successfully (${DOCKER_DURATION}s)"
    else
        print_error "Docker build failed"
        exit 1
    fi
fi

# =============================================
# Summary
# =============================================
TOTAL_DURATION=$(( $(date +%s) - BUILD_START ))

print_header "BUILD COMPLETE"
echo ""
echo -e "  Total time:  ${GREEN}${TOTAL_DURATION}s${NC}"
echo -e "  JARs built:  ${GREEN}${#SERVICES[@]}${NC}"
echo -e "  Docker:      ${GREEN}$BUILD_DOCKER${NC}"
echo ""

echo -e "${YELLOW}Next steps:${NC}"
echo -e "  Start services:  ${GREEN}docker compose up -d${NC}"
echo -e "  View logs:       ${GREEN}docker compose logs -f${NC}"
echo -e "  Stop services:   ${GREEN}docker compose down${NC}"
echo -e "  Run API tests:   ${GREEN}./scripts/test-api.sh${NC}"
echo ""

print_success "All done!"
