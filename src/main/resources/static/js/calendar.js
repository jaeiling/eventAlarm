/**
 * calendar.js - EventAlarm 달력 렌더링
 * eventDataList 는 page.html 에서 인라인으로 주입됨
 */
(function () {
  const today = new Date();
  let curYear = today.getFullYear();
  let curMonth = today.getMonth(); // 0-indexed

  // 행사 날짜 → 행사 목록 맵
  function buildEventMap() {
    const map = {};
    (eventDataList || []).forEach(e => {
      const dateKey = e.datetime.slice(0, 10); // "yyyy-MM-dd"
      if (!map[dateKey]) map[dateKey] = [];
      map[dateKey].push(e);
    });
    return map;
  }

  function renderCalendar() {
    const eventMap = buildEventMap();
    const titleEl = document.getElementById('calTitle');
    const gridEl = document.getElementById('calGrid');
    if (!titleEl || !gridEl) return;

    titleEl.textContent = `${curYear}년 ${curMonth + 1}월`;
    gridEl.innerHTML = '';

    const firstDay = new Date(curYear, curMonth, 1).getDay();
    const lastDate = new Date(curYear, curMonth + 1, 0).getDate();
    const prevLastDate = new Date(curYear, curMonth, 0).getDate();

    // 이전 달 공백 채우기
    for (let i = firstDay - 1; i >= 0; i--) {
      gridEl.appendChild(makeCell(prevLastDate - i, true, false, null));
    }

    // 이번 달
    for (let d = 1; d <= lastDate; d++) {
      const dateKey = `${curYear}-${String(curMonth + 1).padStart(2, '0')}-${String(d).padStart(2, '0')}`;
      const events = eventMap[dateKey] || [];
      const isToday = (d === today.getDate() && curMonth === today.getMonth() && curYear === today.getFullYear());
      gridEl.appendChild(makeCell(d, false, isToday, events.length ? events : null));
    }

    // 다음 달 공백 채우기 (6줄 유지)
    const total = firstDay + lastDate;
    const remainder = total % 7 === 0 ? 0 : 7 - (total % 7);
    for (let i = 1; i <= remainder; i++) {
      gridEl.appendChild(makeCell(i, true, false, null));
    }
  }

  function makeCell(day, otherMonth, isToday, events) {
    const cell = document.createElement('div');
    cell.className = 'cal-cell';
    if (otherMonth) cell.classList.add('other-month');
    if (isToday) cell.classList.add('today');

    // 요일 색상 (실제 날짜만)
    if (!otherMonth) {
      const dow = new Date(curYear, curMonth, day).getDay();
      if (dow === 0) cell.classList.add('sun');
      if (dow === 6) cell.classList.add('sat');
    }

    const numEl = document.createElement('span');
    numEl.textContent = day;
    cell.appendChild(numEl);

    if (events && events.length) {
      cell.classList.add('has-event');

      // 점 표시
      const dot = document.createElement('div');
      dot.className = 'event-dot';
      cell.appendChild(dot);

      // 툴팁 (여러 행사면 줄 바꿈)
      const tooltip = document.createElement('div');
      tooltip.className = 'cal-tooltip';
      tooltip.textContent = events.map(e => e.title).join(' / ');
      cell.appendChild(tooltip);

      // 클릭 → 해당 행사 카드 스크롤 + 모달 열기
      cell.addEventListener('click', () => {
        const firstEvent = events[0];
        const card = document.querySelector(`.event-card[data-id="${firstEvent.id}"]`);
        if (card) {
          card.scrollIntoView({ behavior: 'smooth', block: 'center' });
          setTimeout(() => openModal(card), 300);
        }
      });
    }

    return cell;
  }

  // 월 이동
  document.getElementById('prevMonth')?.addEventListener('click', () => {
    if (--curMonth < 0) { curMonth = 11; curYear--; }
    renderCalendar();
  });
  document.getElementById('nextMonth')?.addEventListener('click', () => {
    if (++curMonth > 11) { curMonth = 0; curYear++; }
    renderCalendar();
  });

  renderCalendar();
})();
