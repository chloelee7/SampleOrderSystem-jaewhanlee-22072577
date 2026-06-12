#!/usr/bin/env bash
# 생산라인 빠른 시연: 더미 데이터 생성 → S-003 주문(40개, 재고 30) → 승인(PRODUCING) → 생산라인 조회 → 11분 경과 → CONFIRMED
set -e
TMPDIR=$(mktemp -d)
printf '7\n2\nS-003\nDemo Lab\n40\n3\n1\nORD-0001\n5\n1\n5\n3\n11\n0\n' | \
  ./gradlew -q run --args="--seed-dummy --data-dir $TMPDIR"
