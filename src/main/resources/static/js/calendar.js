/**
 * calendar.js - EventAlarm 달력 렌더링 (띠 형식, 최대 3줄)
 */
(function () {
  const today = new Date();
  let curYear = today.getFullYear();
  let curMonth = today.getMonth();

  const MAX_LANES = 3;
  const COLORS = [
    { bg: '#e53935', text: '#fff' },
    { bg: '#1E88E5', text: '#fff' },
    { bg: '#43A047', text: '#fff' },
    { bg: '#FB8C00', text: '#fff' },
    { bg: '#8E24AA', text: '#fff' },
    { bg: '#00ACC1', text: '#fff' },
  ];

  function toDay(str) {
    if (!str) return null;
    const d = new Date(str.replace(' ', 'T'));
    return new Date(d.getFullYear(), d.getMonth(), d.getDate());
  }

  function dateKey(y, m, d) {
    return `${y}-${String(m + 1).padStart(2, '0')}-${String(d).padStart(2, '0')}`;
  }

  function renderCalendar() {
    const titleEl = document.getElementById('calTitle');
    const gridEl  = document.getElementById('calGrid');
    if (!titleEl || !gridEl) return;

    titleEl.textContent = `${curYear}년 ${curMonth + 1}월`;
    gridEl.innerHTML = '';

    const firstDay     = new Date(curYear, curMonth, 1).getDay();
    const lastDate     = new Date(curYear, curMonth + 1, 0).getDate();
    const prevLastDate = new Date(curYear, curMonth, 0).getDate();

    // 셀 배열
    const cells = [];
    for (let i = firstDay - 1; i >= 0; i--)
      cells.push({ day: prevLastDate - i, otherMonth: true, dateStr: null });
    for (let d = 1; d <= lastDate; d++)
      cells.push({ day: d, otherMonth: false, dateStr: dateKey(curYear, curMonth, d) });
    const rem = cells.length % 7 === 0 ? 0 : 7 - (cells.length % 7);
    for (let i = 1; i <= rem; i++)
      cells.push({ day: i, otherMonth: true, dateStr: null });

    // 셀 인덱스 맵
    const dateToIdx = {};
    cells.forEach((c, i) => { if (c.dateStr) dateToIdx[c.dateStr] = i; });

    // 행사 → segments
    const events = (eventDataList || []);
    const assignments = [];
    const rowLanes = Array.from({ length: Math.ceil(cells.length / 7) }, () => []);

    events.forEach((e, eIdx) => {
      const start = toDay(e.datetime);
      const end   = e.enddatetime ? toDay(e.enddatetime) : start;
      if (!start) return;

      const monthStart = new Date(curYear, curMonth, 1);
      const monthEnd   = new Date(curYear, curMonth + 1, 0);
      const clipStart  = start < monthStart ? monthStart : start;
      const clipEnd    = end   > monthEnd   ? monthEnd   : end;
      if (clipStart > clipEnd) return;

      const sk = dateKey(clipStart.getFullYear(), clipStart.getMonth(), clipStart.getDate());
      const ek = dateKey(clipEnd.getFullYear(),   clipEnd.getMonth(),   clipEnd.getDate());
      const startIdx = dateToIdx[sk];
      const endIdx   = dateToIdx[ek];
      if (startIdx === undefined || endIdx === undefined) return;

      const color = COLORS[eIdx % COLORS.length];

      let segStart = startIdx;
      while (segStart <= endIdx) {
        const rowIdx  = Math.floor(segStart / 7);
        const rowEnd  = rowIdx * 7 + 6;
        const segEnd  = Math.min(endIdx, rowEnd);
        const isFirst = segStart === startIdx;
        const isLast  = segEnd   === endIdx;

        // lane 배정
        let lane = 0;
        while (lane < MAX_LANES &&
               rowLanes[rowIdx].some(a => a.segStart <= segEnd && a.segEnd >= segStart && a.lane === lane)) {
          lane++;
        }
        if (lane < MAX_LANES) {
          rowLanes[rowIdx].push({ segStart, segEnd, lane });
          assignments.push({ event: e, segStart, segEnd, lane, color, isFirst, isLast });
        }
        segStart = segEnd + 1;
      }
    });

    // DOM 구성
    const weekCount = cells.length / 7;
    for (let row = 0; row < weekCount; row++) {
      const rowEl = document.createElement('div');
      rowEl.className = 'cal-row';

      for (let col = 0; col < 7; col++) {
        const idx    = row * 7 + col;
        const cell   = cells[idx];
        const cellEl = document.createElement('div');
        cellEl.className = 'cal-cell';
        if (cell.otherMonth) cellEl.classList.add('other-month');

        const isToday = !cell.otherMonth && cell.day === today.getDate()
                      && curMonth === today.getMonth() && curYear === today.getFullYear();
        if (isToday) cellEl.classList.add('today');

        if (!cell.otherMonth) {
          const dow = new Date(curYear, curMonth, cell.day).getDay();
          if (dow === 0) cellEl.classList.add('sun');
          if (dow === 6) cellEl.classList.add('sat');
        }

        const numEl = document.createElement('span');
        numEl.className = 'cal-day-num';
        numEl.textContent = cell.day;
        cellEl.appendChild(numEl);
        rowEl.appendChild(cellEl);
      }

      // 띠 오버레이
      const bandsEl = document.createElement('div');
      bandsEl.className = 'cal-bands';

      const rowAssignments = assignments.filter(a => Math.floor(a.segStart / 7) === row);
      const maxLane = rowAssignments.length > 0
        ? Math.max(...rowAssignments.map(a => a.lane)) : -1;
      rowEl.style.minHeight = `${52 + (maxLane + 1) * 20}px`;

      rowAssignments.forEach(a => {
        const col0     = a.segStart % 7;
        const col1     = a.segEnd   % 7;
        const leftPct  = (col0 / 7) * 100;
        const widthPct = ((col1 - col0 + 1) / 7) * 100;
        const topPx    = 30 + a.lane * 20;

        const band = document.createElement('div');
        band.className = 'cal-band';
        band.style.cssText = `
          left: calc(${leftPct}% + ${a.isFirst ? 3 : 0}px);
          width: calc(${widthPct}% - ${(a.isFirst ? 3 : 0) + (a.isLast ? 3 : 0)}px);
          top: ${topPx}px;
          background: ${a.color.bg};
          color: ${a.color.text};
          border-radius: ${a.isFirst ? '6px' : '0'} ${a.isLast ? '6px' : '0'} ${a.isLast ? '6px' : '0'} ${a.isFirst ? '6px' : '0'};
        `;
        if (a.isFirst) band.textContent = a.event.title;
        band.title = a.event.title;
        band.style.cursor = 'pointer';
        band.addEventListener('click', (ev) => {
          ev.stopPropagation();
          const card = document.querySelector(`.event-card[data-id="${a.event.id}"]`);
          if (card) openModal(card);
          else openModalFromData(a.event);
        });
        bandsEl.appendChild(band);
      });

      rowEl.appendChild(bandsEl);
      gridEl.appendChild(rowEl);
    }
  }

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
