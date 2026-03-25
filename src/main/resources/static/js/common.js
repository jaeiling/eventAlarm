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
