# Hitboxing
[![license](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

Interactive multimedia concept. Third of a kind.

Move your rectangle with arrow keys on your keyboard and try to collide with other rectangles!

# Instructions

You should run DesktopLauncher main method.

It is Java (8+ ???) project with dependencies described using Gradle.

# Main Loop

Phase 1 - setup
    set start position for player
    load user image
    create default rectangles/trapeziums

Phase 2 - Loop
    input handling
    should exit?
    collision detection
    state change
    clean screen
    render stuff

Phase 3 - exit with resource cleanup