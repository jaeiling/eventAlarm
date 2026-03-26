/**
 * modal.js - 행사 상세 모달 & D-day 뱃지
 */

// ── D-day 뱃지 렌더링 ──────────────────────────────────────────
document.querySelectorAll('.dday-badge').forEach(badge => {
  const datetimeStr = badge.dataset.datetime; // "2025-04-10T18:00:00" 형태
  if (!datetimeStr) return;

  const eventDate = new Date(datetimeStr);
  const now = new Date();

  const diffMs = eventDate - now;
  const diffDays = Math.ceil(diffMs / (1000 * 60 * 60 * 24));

  if (diffDays > 0) {
    badge.textContent = `D-${diffDays}`;
    if (diffDays <= 3) {
      badge.classList.add('dday-red');    // 3일 이내 → 빨강
    } else if (diffDays <= 7) {
      badge.classList.add('dday-yellow'); // 4~7일 → 노랑
    } else {
      badge.classList.add('dday-green');  // 8일 이상 → 초록
    }
  } else if (diffDays === 0) {
    badge.textContent = 'D-Day';
    badge.classList.add('dday-today');
  } else {
    badge.textContent = `D+${Math.abs(diffDays)}`;
    badge.classList.add('dday-past');
  }
});

// ── 모달 열기/닫기 ──────────────────────────────────────────────
function openModal(cardEl) {
  const title    = cardEl.dataset.title    || '';
  const datetime = cardEl.dataset.datetime || '';
  const location = cardEl.dataset.location || '';
  const address  = cardEl.dataset.address  || '';
  const desc     = cardEl.dataset.desc     || '';
  const fee      = cardEl.dataset.fee      || '';
  const account  = cardEl.dataset.account  || '';

  document.getElementById('modalTitle').textContent    = title;
  document.getElementById('modalDatetime').textContent = datetime;
  document.getElementById('modalLocation').textContent = location;

  // 지도 버튼
  const mapBtns = document.getElementById('mapBtns');
  mapBtns.innerHTML = '';
  const searchQuery = encodeURIComponent(address || location);
  if (searchQuery) {
    mapBtns.innerHTML = `
      <a href="https://map.kakao.com/link/search/${searchQuery}" target="_blank" class="map-btn map-btn-kakao">
        <img src="https://t1.kakaocdn.net/kakaocorp/kakaocorp/admin/asset/corporation/20240424094419.svg" alt="kakao" class="map-btn-icon"> 카카오맵
      </a>
      <a href="https://map.naver.com/v5/search/${searchQuery}" target="_blank" class="map-btn map-btn-naver">
        <img src="https://ssl.pstatic.net/static/maps/mantle/map-icons/favicon-32x32.png" alt="naver" class="map-btn-icon"> 네이버지도
      </a>
    `;
  }

  // 설명
  const descRow = document.getElementById('descRow');
  if (desc) {
    document.getElementById('modalDesc').textContent = desc;
    descRow.style.display = 'flex';
  } else {
    descRow.style.display = 'none';
  }

  // 회비 & 계좌
  const feeRow     = document.getElementById('feeRow');
  const accountWrap = document.getElementById('accountWrap');
  if (fee) {
    document.getElementById('modalFee').textContent = fee;
    feeRow.style.display = 'flex';
    if (account) {
      document.getElementById('modalAccount').textContent = account;
      accountWrap.style.display = 'flex';
      // 복사 버튼에 계좌 저장
      document.getElementById('copyAccountBtn').dataset.account = account;
    } else {
      accountWrap.style.display = 'none';
    }
  } else {
    feeRow.style.display = 'none';
  }

  document.getElementById('modalOverlay').classList.add('open');
  document.getElementById('modalSheet').classList.add('open');
  document.body.style.overflow = 'hidden';
}

function closeModal() {
  document.getElementById('modalOverlay').classList.remove('open');
  document.getElementById('modalSheet').classList.remove('open');
  document.body.style.overflow = '';
}

function copyAccount() {
  const account = document.getElementById('copyAccountBtn').dataset.account;
  if (!account) return;
  navigator.clipboard.writeText(account).then(() => {
    const btn = document.getElementById('copyAccountBtn');
    btn.textContent = '복사됨✓';
    btn.style.background = '#d4edda';
    setTimeout(() => { btn.textContent = '복사'; btn.style.background = ''; }, 2000);
  });
}

// ESC 키로 모달 닫기
document.addEventListener('keydown', e => {
  if (e.key === 'Escape') closeModal();
});
