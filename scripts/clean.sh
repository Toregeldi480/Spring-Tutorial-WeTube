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

# Navigate to project root
cd "$PROJECT_ROOT" || exit 1

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

# Function to print warning
print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

# Function to print info
print_info() {
    echo -e "${YELLOW}→ $1${NC}"
}

# Function to confirm action
confirm() {
    read -r -p "$(echo -e ${RED}"$1 [y/N]: "${NC})" response
    case "$response" in
        [yY][eE][sS]|[yY]) 
            return 0
            ;;
        *)
            return 1
            ;;
    esac
}

# =============================================
# Start
# =============================================
clear
echo -e "${RED}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${RED}║${NC}        ${YELLOW}WeTube Cleanup Script${NC}                       ${RED}║${NC}"
echo -e "${RED}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

print_warning "This will remove ALL data including:"
echo -e "  • All Docker containers"
echo -e "  • All Docker volumes"
echo -e "  • ${RED}Database data (users, videos, etc.)${NC}"
echo -e "  • ${RED}Uploaded videos${NC}"
echo -e "  • ${RED}Kafka/Zookeeper data${NC}"
echo ""

# =============================================
# Confirmation
# =============================================
if ! confirm "Are you sure you want to continue?"; then
    echo -e "\n${GREEN}Cleanup cancelled${NC}"
    exit 0
fi

echo ""

# =============================================
# Stop and remove containers
# =============================================
print_header "Stopping Docker Containers"

print_info "Running docker compose down..."
docker compose down -v 2>/dev/null

if [ $? -eq 0 ]; then
    print_success "Containers stopped and removed"
else
    print_warning "Some containers may not have been running"
fi

# =============================================
# Find and remove volumes
# =============================================
print_header "Removing Docker Volumes"

# Find all volumes related to this project
VOLUMES=$(docker volume ls -q | grep -i "wetube\|kafka\|zookeeper\|postgres\|video-storage" 2>/dev/null)

REMOVED=0
SKIPPED=0

if [ -z "$VOLUMES" ]; then
    print_warning "No matching volumes found"
    print_info "Looking for volumes with different names..."
    
    # List all volumes to help debug
    echo ""
    docker volume ls
    echo ""
    
    # Try common volume names
    for vol in $(docker volume ls -q); do
        if echo "$vol" | grep -qiE "wetube|kafka|zookeeper|postgres|video"; then
            print_info "Found volume: $vol"
            if docker volume rm "$vol" 2>/dev/null; then
                print_success "Removed: $vol"
                ((REMOVED++))
            else
                print_error "Failed to remove: $vol"
            fi
        fi
    done
else
    # Remove found volumes
    for VOLUME in $VOLUMES; do
        print_info "Removing volume: ${CYAN}$VOLUME${NC}..."
        if docker volume rm "$VOLUME" > /dev/null 2>&1; then
            print_success "Removed: $VOLUME"
            ((REMOVED++))
        else
            print_error "Failed to remove: $VOLUME"
        fi
    done
fi

if [ $REMOVED -eq 0 ] && [ -z "$VOLUMES" ]; then
    print_warning "No volumes were removed"
    echo ""
    echo -e "${YELLOW}To manually remove volumes, list them with:${NC}"
    echo -e "  ${GREEN}docker volume ls${NC}"
    echo ""
    echo -e "${YELLOW}Then remove with:${NC}"
    echo -e "  ${GREEN}docker volume rm <volume-name>${NC}"
fi

# =============================================
# Clean up orphaned volumes
# =============================================
print_header "Cleaning Up Orphaned Volumes"

ORPHANED=$(docker volume ls -qf dangling=true)
if [ -n "$ORPHANED" ]; then
    print_info "Removing dangling volumes..."
    docker volume rm $ORPHANED > /dev/null 2>&1
    print_success "Orphaned volumes removed"
else
    print_info "No orphaned volumes found"
fi

# =============================================
# Summary
# =============================================
print_header "CLEANUP COMPLETE"

echo ""
echo -e "  Volumes removed:  ${GREEN}$REMOVED${NC}"
echo ""

echo -e "${YELLOW}Next steps:${NC}"
echo -e "  Rebuild:  ${GREEN}./scripts/build.sh${NC}"
echo -e "  Start:    ${GREEN}docker compose up -d${NC}"
echo ""
