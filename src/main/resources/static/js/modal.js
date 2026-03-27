/**
 * modal.js - 행사 상세 모달 & D-day 뱃지
 */

// ── D-day 뱃지 렌더링 ──────────────────────────────────────────
document.querySelectorAll('.dday-badge').forEach(badge => {
  const datetimeStr = badge.dataset.datetime;
  if (!datetimeStr) return;

  const eventDate = new Date(datetimeStr);
  const now = new Date();
  const eventMidnight = new Date(eventDate.getFullYear(), eventDate.getMonth(), eventDate.getDate());
  const todayMidnight = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  const diffDays = Math.round((eventMidnight - todayMidnight) / (1000 * 60 * 60 * 24));

  if (diffDays > 0) {
    badge.textContent = `D-${diffDays}`;
    if (diffDays <= 3)      badge.classList.add('dday-red');
    else if (diffDays <= 7) badge.classList.add('dday-yellow');
    else                    badge.classList.add('dday-green');
  } else if (diffDays === 0) {
    badge.textContent = 'D-Day';
    badge.classList.add('dday-today');
  } else {
    badge.textContent = `D+${Math.abs(diffDays)}`;
    badge.classList.add('dday-past');
  }
});

// ── 캐러셀 빌드 ─────────────────────────────────────────────────
function buildCarousel(images) {
  const wrap = document.getElementById('modalCarouselWrap');
  if (!images || images.length === 0) {
    wrap.style.display = 'none';
    return;
  }

  const slides = images.map((url, i) =>
    `<div class="modal-carousel-slide">
      <img src="${url.replace(/"/g, '&quot;')}" alt="이미지 ${i + 1}" loading="lazy"/>
    </div>`
  ).join('');

  const arrows = images.length > 1 ? `
    <button class="carousel-arrow carousel-arrow-left" onclick="shiftCarousel(this,-1)">&#8249;</button>
    <button class="carousel-arrow carousel-arrow-right" onclick="shiftCarousel(this,1)">&#8250;</button>
  ` : '';

  const dots = images.length > 1
    ? `<div class="modal-dots">${
        images.map((_, i) =>
          `<span class="modal-dot${i === 0 ? ' active' : ''}"></span>`
        ).join('')
      }</div>`
    : '';

  wrap.innerHTML = `<div class="modal-carousel-inner"><div class="modal-carousel" id="modalCarousel">${slides}</div>${arrows}</div>${dots}`;
  wrap.style.display = 'block';

  if (images.length > 1) {
    const track = wrap.querySelector('.modal-carousel');
    const dotEls = wrap.querySelectorAll('.modal-dot');
    const leftBtn = wrap.querySelector('.carousel-arrow-left');
    const rightBtn = wrap.querySelector('.carousel-arrow-right');

    const updateArrows = (idx) => {
      if (leftBtn)  leftBtn.style.opacity  = idx === 0 ? '0.3' : '1';
      if (rightBtn) rightBtn.style.opacity = idx === images.length - 1 ? '0.3' : '1';
    };

    track.addEventListener('scroll', () => {
      const idx = Math.round(track.scrollLeft / track.offsetWidth);
      dotEls.forEach((d, i) => d.classList.toggle('active', i === idx));
      updateArrows(idx);
    }, { passive: true });

    updateArrows(0);
  }
}

// ── 모달 열기 ────────────────────────────────────────────────────
function openModal(cardEl) {
  if (!cardEl) return;
  const postType    = cardEl.dataset.type      || 'EVENT';
  const isNotice    = postType === 'NOTICE';
  const title       = cardEl.dataset.title     || '';
  const datetime    = cardEl.dataset.datetime  || '';
  const endDatetime = cardEl.dataset.enddatetime || '';
  const location    = cardEl.dataset.location  || '';
  const address     = cardEl.dataset.address   || '';
  const desc        = cardEl.dataset.desc      || '';
  const fee         = cardEl.dataset.fee       || '';
  const account     = cardEl.dataset.account   || '';
  const link        = cardEl.dataset.link      || '';
  const imagesRaw   = cardEl.dataset.images    || '';
  const thumbnail   = cardEl.dataset.thumbnail || '';

  // 이미지 목록 (|구분자, 없으면 thumbnail 폴백)
  let images = imagesRaw ? imagesRaw.split('|').filter(Boolean) : [];
  if (images.length === 0 && thumbnail) images = [thumbnail];

  // 헤더 타입 뱃지
  const headerType = document.getElementById('modalHeaderType');
  headerType.textContent = isNotice ? '공지' : '행사';
  headerType.className = 'modal-header-type ' + (isNotice ? 'type-notice' : 'type-event');

  // 제목
  document.getElementById('modalTitle').textContent = title;

  // 캐러셀
  buildCarousel(images);

  // 날짜/장소 — 공지글이면 숨김
  const datetimeRow = document.getElementById('datetimeRow');
  const locationRow = document.getElementById('locationRow');
  if (isNotice) {
    datetimeRow.style.display = 'none';
    locationRow.style.display = 'none';
  } else {
    datetimeRow.style.display = '';
    locationRow.style.display = '';

    document.getElementById('modalDatetime').textContent =
      endDatetime ? datetime + '  ~  ' + endDatetime : datetime;

    document.getElementById('modalLocation').textContent = location;

    const mapBtns = document.getElementById('mapBtns');
    mapBtns.innerHTML = '';
    const searchQuery = encodeURIComponent(address || location);
    if (searchQuery) {
      mapBtns.innerHTML = `
        <a href="https://map.kakao.com/link/search/${searchQuery}" target="_blank" class="map-btn map-btn-kakao">
          <span class="map-btn-logo">K</span> 카카오맵
        </a>
        <a href="https://map.naver.com/v5/search/${searchQuery}" target="_blank" class="map-btn map-btn-naver">
          <span class="map-btn-logo">N</span> 네이버지도
        </a>
      `;
    }
  }

  // 설명
  const descRow = document.getElementById('descRow');
  if (desc) {
    document.getElementById('modalDesc').textContent = desc;
    descRow.style.display = 'block';
  } else {
    descRow.style.display = 'none';
  }

  // 회비 & 계좌
  const feeRow      = document.getElementById('feeRow');
  const accountWrap = document.getElementById('accountWrap');
  if (!isNotice && fee) {
    document.getElementById('modalFee').textContent = fee;
    feeRow.style.display = 'flex';
    if (account) {
      document.getElementById('modalAccount').textContent = account;
      accountWrap.style.display = 'flex';
      document.getElementById('copyAccountBtn').dataset.account = account;
    } else {
      accountWrap.style.display = 'none';
    }
  } else {
    feeRow.style.display = 'none';
  }

  // 링크 버튼
  const linkEl = document.getElementById('modalLink');
  if (link) {
    linkEl.href = link;
    try {
      const url = new URL(link);
      document.getElementById('modalLinkText').textContent =
        url.hostname.replace('www.', '') + (url.pathname !== '/' ? url.pathname : '');
    } catch {
      document.getElementById('modalLinkText').textContent = '링크 바로가기';
    }
    linkEl.style.display = 'flex';
  } else {
    linkEl.style.display = 'none';
  }

  document.getElementById('modalOverlay').classList.add('open');
  document.getElementById('modalSheet').classList.add('open');
  document.body.style.overflow = 'hidden';

  // 뒤로가기로 모달 닫기 (모바일 대응)
  history.pushState({ modal: true }, '');
}

/** 달력에서 DOM 카드 없이 데이터 객체로 직접 모달 열기 */
function openModalFromData(data) {
  const fake = document.createElement('div');
  fake.dataset.type        = data.type        || 'EVENT';
  fake.dataset.title       = data.title       || '';
  fake.dataset.datetime    = data.datetime    || '';
  fake.dataset.enddatetime = data.enddatetime || '';
  fake.dataset.location    = data.location    || '';
  fake.dataset.address     = data.address     || '';
  fake.dataset.desc        = data.desc        || '';
  fake.dataset.fee         = data.fee         || '';
  fake.dataset.account     = data.account     || '';
  fake.dataset.link        = data.link        || '';
  fake.dataset.thumbnail   = data.thumbnail   || '';
  fake.dataset.images      = data.images      || '';
  openModal(fake);
}

function shiftCarousel(btn, dir) {
  const track = btn.closest('.modal-carousel-inner').querySelector('.modal-carousel');
  track.scrollBy({ left: dir * track.offsetWidth, behavior: 'smooth' });
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

// 모바일 뒤로가기로 모달 닫기
window.addEventListener('popstate', e => {
  if (document.getElementById('modalSheet').classList.contains('open')) {
    closeModal();
  }
});
