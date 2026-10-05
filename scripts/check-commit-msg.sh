#!/usr/bin/env bash
# 커밋 메시지 첫 줄이 README 의 커밋 컨벤션을 따르는지 검사한다.
#   형식: <이모지> [<타입>] <요약>   (요약 50자 이내, 마침표로 끝나지 않음)
# 사용: scripts/check-commit-msg.sh <커밋 메시지 파일>
# git commit-msg 훅과 CI 에서 함께 쓴다.
set -euo pipefail
export LC_ALL=C.UTF-8

subject=$(head -n 1 "$1")

declare -A emoji_of=(
  [init]="🎉" [feat]="✨" [fix]="🐛" [refactor]="♻" [test]="✅"
  [docs]="📝" [style]="🎨" [chore]="🔧" [remove]="🔥" [deploy]="🚀"
)

fail() {
  echo "커밋 메시지 형식 오류: $1" >&2
  echo "  메시지: $subject" >&2
  echo "  형식:   <이모지> [<타입>] <요약>   예) ✨ [feat] quick chat 입력창에 전송 기록 추가" >&2
  exit 1
}

# git 이 자동으로 만드는 메시지는 건너뛴다.
case "$subject" in
  "Merge "* | "Revert "* | "fixup! "* | "squash! "*) exit 0 ;;
esac

if [[ ! "$subject" =~ ^([^ ]+)\ \[([a-z]+)\]\ (.+)$ ]]; then
  fail "'<이모지> [<타입>] <요약>' 모양이 아닙니다."
fi

emoji=${BASH_REMATCH[1]//$'️'/}
type=${BASH_REMATCH[2]}
summary=${BASH_REMATCH[3]}

expected=${emoji_of[$type]:-}
if [[ -z "$expected" ]]; then
  fail "알 수 없는 타입 [$type] 입니다. (${!emoji_of[*]})"
fi
if [[ "$emoji" != "$expected" ]]; then
  fail "[$type] 의 이모지는 $expected 입니다."
fi
if (( ${#summary} > 50 )); then
  fail "요약이 ${#summary}자입니다. 50자 이내로 줄여 주세요."
fi
if [[ "$summary" == *. ]]; then
  fail "요약 끝에 마침표를 붙이지 않습니다."
fi
