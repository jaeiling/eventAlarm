/**
 * common.js - 공통 유틸
 */

// 계좌 복사 (랜딩/관리자 페이지용 전역 함수)
function copySerial() {
  const el = document.getElementById('serialNumber');
  if (!el) return;
  navigator.clipboard.writeText(el.textContent.trim()).then(() => {
    alert('관리 번호가 복사되었습니다!');
  });
}

/**
 * 회비 입력 포맷: 숫자만 입력 → 자동으로 3자리 쉼표 + '원' 붙임
 * 저장 시에는 숫자+원 형태로 hidden input에 넣음
 */
function formatFee(input) {
  // 숫자 이외 제거
  let raw = input.value.replace(/[^0-9]/g, '');
  if (!raw) {
    input.value = '';
    return;
  }
  // 3자리 쉼표 포맷
  const formatted = Number(raw).toLocaleString('ko-KR');
  input.value = formatted + '원';

  // 커서를 '원' 앞으로 이동
  const pos = input.value.length - 1;
  requestAnimationFrame(() => {
    input.setSelectionRange(pos, pos);
  });
}

// 페이지 로드 시 기존 회비 값 포맷
document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('input[oninput="formatFee(this)"]').forEach(input => {
    if (input.value) {
      const raw = input.value.replace(/[^0-9]/g, '');
      if (raw) input.value = Number(raw).toLocaleString('ko-KR') + '원';
    }
  });
});
