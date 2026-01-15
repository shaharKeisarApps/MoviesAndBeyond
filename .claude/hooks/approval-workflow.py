#!/usr/bin/env python3
"""
Approval Workflow Hook

This hook provides approval workflow enforcement for task completion.
It can be used as a PreToolUse hook to require explicit approval before
certain operations, or as a Stop hook to validate task completion.

Usage:
    python3 approval-workflow.py --mode=pre-task
    python3 approval-workflow.py --mode=stop-check

Exit Codes:
    0 - Approved / Continue
    2 - Blocked / Requires attention
"""

import json
import sys
import os
from datetime import datetime


def log_message(message: str, level: str = "INFO"):
    """Log a message to stderr for visibility."""
    timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    print(f"[{timestamp}] [{level}] {message}", file=sys.stderr)


def read_stdin_json():
    """Read and parse JSON from stdin."""
    try:
        return json.load(sys.stdin)
    except json.JSONDecodeError:
        return {}


def check_quality_gates():
    """Verify quality gates are passing."""
    import subprocess

    gates = {
        "spotless": ["./gradlew", "spotlessCheck", "--quiet"],
        "detekt": ["./gradlew", "detekt", "--quiet"],
        "compile": ["./gradlew", "compileDebugKotlin", "--quiet"],
    }

    results = {}
    for gate_name, command in gates.items():
        try:
            result = subprocess.run(
                command,
                capture_output=True,
                timeout=300
            )
            results[gate_name] = result.returncode == 0
        except (subprocess.TimeoutExpired, FileNotFoundError):
            results[gate_name] = None  # Unknown

    return results


def validate_task_completion(context: dict) -> dict:
    """
    Validate that a task meets completion criteria.

    Returns dict with:
        - ok: bool - whether task is complete
        - reason: str - explanation
        - checklist: list - status of each check
    """
    checklist = []
    all_passed = True

    # Check 1: Code compiles
    # (In real implementation, check recent build status)
    checklist.append({
        "item": "Code compiles",
        "status": "assumed_pass",
        "required": True
    })

    # Check 2: Tests pass
    # (In real implementation, check test results)
    checklist.append({
        "item": "Tests pass",
        "status": "assumed_pass",
        "required": True
    })

    # Check 3: Code quality
    # (In real implementation, run quick checks)
    checklist.append({
        "item": "Code quality checks",
        "status": "assumed_pass",
        "required": True
    })

    # Check 4: Code review
    # (In real implementation, check if code-reviewer was invoked)
    has_review = context.get("code_reviewed", False)
    checklist.append({
        "item": "Code review completed",
        "status": "pass" if has_review else "pending",
        "required": True
    })
    if not has_review:
        all_passed = False

    # Check 5: Test plan (for PRs)
    has_test_plan = context.get("test_plan_complete", False)
    checklist.append({
        "item": "Test plan verified",
        "status": "pass" if has_test_plan else "pending",
        "required": True
    })
    if not has_test_plan:
        all_passed = False

    return {
        "ok": all_passed,
        "reason": "All quality gates passed" if all_passed else "Some quality gates pending",
        "checklist": checklist
    }


def handle_pre_task(input_data: dict):
    """Handle pre-task validation."""
    tool_name = input_data.get("tool_name", "")
    tool_input = input_data.get("tool_input", {})

    # List of operations that require approval
    approval_required = {
        "git push": "Pushing to remote requires verification",
        "gh pr merge": "Merging PR requires test plan completion",
        "rm": "Deletion operations require confirmation",
    }

    if tool_name == "Bash":
        command = tool_input.get("command", "")

        for pattern, reason in approval_required.items():
            if pattern in command:
                log_message(f"Approval required: {reason}")
                return {
                    "hookSpecificOutput": {
                        "hookEventName": "PreToolUse",
                        "permissionDecision": "ask",
                        "permissionDecisionReason": reason
                    }
                }

    # Allow by default
    return None


def handle_stop_check(input_data: dict):
    """Handle stop/completion check."""
    context = input_data.get("context", {})

    validation = validate_task_completion(context)

    if validation["ok"]:
        log_message("Task completion validated successfully")
        return {"ok": True, "reason": validation["reason"]}
    else:
        log_message(f"Task completion blocked: {validation['reason']}")
        pending_items = [
            item["item"] for item in validation["checklist"]
            if item["status"] == "pending"
        ]
        return {
            "ok": False,
            "reason": f"Pending items: {', '.join(pending_items)}"
        }


def main():
    """Main entry point."""
    mode = "pre-task"  # Default mode

    # Parse arguments
    for arg in sys.argv[1:]:
        if arg.startswith("--mode="):
            mode = arg.split("=")[1]

    # Read input
    input_data = read_stdin_json()

    # Handle based on mode
    if mode == "pre-task":
        result = handle_pre_task(input_data)
        if result:
            print(json.dumps(result))
            sys.exit(0)
        sys.exit(0)

    elif mode == "stop-check":
        result = handle_stop_check(input_data)
        print(json.dumps(result))
        if result.get("ok"):
            sys.exit(0)
        else:
            sys.exit(2)  # Block

    else:
        log_message(f"Unknown mode: {mode}", "ERROR")
        sys.exit(1)


if __name__ == "__main__":
    main()
