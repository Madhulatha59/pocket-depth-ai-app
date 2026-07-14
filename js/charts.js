/* ============================================================
   charts.js — HTML5 Canvas Chart Renderers
   Custom widgets mirroring the Compose Canvas UI
   ============================================================ */

const ChartService = (() => {
  /**
   * Draws a smooth activity line trend on the dashboard
   */
  function drawActivityTrend(canvasId, dataPoints = [4, 6, 5, 8, 7, 9, 10]) {
    const canvas = document.getElementById(canvasId);
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    
    // Adjust size for High DPI screens
    const width = canvas.clientWidth;
    const height = canvas.clientHeight;
    canvas.width = width * window.devicePixelRatio;
    canvas.height = height * window.devicePixelRatio;
    ctx.scale(window.devicePixelRatio, window.devicePixelRatio);

    ctx.clearRect(0, 0, width, height);

    if (dataPoints.length === 0) return;

    const maxVal = Math.max(...dataPoints, 10);
    const minVal = 0;
    const padding = 20;
    const graphWidth = width - padding * 2;
    const graphHeight = height - padding * 2;

    // Draw grid lines
    ctx.strokeStyle = '#E2E8F0';
    ctx.lineWidth = 1;
    for (let i = 0; i <= 4; i++) {
      const y = padding + (graphHeight * i / 4);
      ctx.beginPath();
      ctx.moveTo(padding, y);
      ctx.lineTo(width - padding, y);
      ctx.stroke();
    }

    // Prepare path points
    const points = dataPoints.map((val, index) => {
      const x = padding + (graphWidth * index / (dataPoints.length - 1));
      const y = padding + graphHeight - ((val - minVal) / (maxVal - minVal) * graphHeight);
      return { x, y };
    });

    // Draw gradient area under the curve
    const areaGradient = ctx.createLinearGradient(0, padding, 0, height - padding);
    areaGradient.addColorStop(0, 'rgba(123, 97, 255, 0.25)');
    areaGradient.addColorStop(1, 'rgba(93, 72, 209, 0.00)');

    ctx.beginPath();
    ctx.moveTo(points[0].x, height - padding);
    
    // Draw smooth curve using bezier controls
    ctx.lineTo(points[0].x, points[0].y);
    for (let i = 0; i < points.length - 1; i++) {
      const p0 = points[i];
      const p1 = points[i + 1];
      const cpX1 = p0.x + (p1.x - p0.x) / 2;
      const cpY1 = p0.y;
      const cpX2 = p0.x + (p1.x - p0.x) / 2;
      const cpY2 = p1.y;
      ctx.bezierCurveTo(cpX1, cpY1, cpX2, cpY2, p1.x, p1.y);
    }
    ctx.lineTo(points[points.length - 1].x, height - padding);
    ctx.closePath();
    ctx.fillStyle = areaGradient;
    ctx.fill();

    // Draw main stroke curve
    const lineGradient = ctx.createLinearGradient(0, 0, width, 0);
    lineGradient.addColorStop(0, '#7B61FF');
    lineGradient.addColorStop(1, '#5D48D1');

    ctx.beginPath();
    ctx.moveTo(points[0].x, points[0].y);
    for (let i = 0; i < points.length - 1; i++) {
      const p0 = points[i];
      const p1 = points[i + 1];
      const cpX1 = p0.x + (p1.x - p0.x) / 2;
      const cpY1 = p0.y;
      const cpX2 = p0.x + (p1.x - p0.x) / 2;
      const cpY2 = p1.y;
      ctx.bezierCurveTo(cpX1, cpY1, cpX2, cpY2, p1.x, p1.y);
    }
    ctx.strokeStyle = lineGradient;
    ctx.lineWidth = 3.5;
    ctx.lineCap = 'round';
    ctx.lineJoin = 'round';
    ctx.stroke();

    // Draw data point circles
    points.forEach((p, idx) => {
      ctx.beginPath();
      ctx.arc(p.x, p.y, 5, 0, Math.PI * 2);
      ctx.fillStyle = '#FFFFFF';
      ctx.fill();
      ctx.strokeStyle = '#5D48D1';
      ctx.lineWidth = 2.5;
      ctx.stroke();
    });
  }

  /**
   * Draws a beautiful probability confidence donut chart
   */
  function drawDonutChart(canvasId, healthy = 90, moderate = 8, severe = 2) {
    const canvas = document.getElementById(canvasId);
    if (!canvas) return;
    const ctx = canvas.getContext('2d');

    const width = canvas.clientWidth;
    const height = canvas.clientHeight;
    canvas.width = width * window.devicePixelRatio;
    canvas.height = height * window.devicePixelRatio;
    ctx.scale(window.devicePixelRatio, window.devicePixelRatio);

    ctx.clearRect(0, 0, width, height);

    const centerX = width / 2;
    const centerY = height / 2;
    const radius = Math.min(width, height) / 2 - 12;
    const thickness = 14;

    const total = healthy + moderate + severe;
    if (total === 0) return;

    // Ported color tokens
    const segments = [
      { percentage: healthy, color: '#4CAF50' },   // Healthy -> Green
      { percentage: moderate, color: '#FF9800' },  // Moderate -> Orange
      { percentage: severe, color: '#D32F2F' }     // Severe -> Red
    ];

    let startAngle = -Math.PI / 2;

    // Draw background circle
    ctx.beginPath();
    ctx.arc(centerX, centerY, radius, 0, Math.PI * 2);
    ctx.strokeStyle = '#F0F2FF';
    ctx.lineWidth = thickness;
    ctx.stroke();

    // Draw segments
    segments.forEach(seg => {
      if (seg.percentage <= 0) return;

      const sliceAngle = (seg.percentage / total) * Math.PI * 2;
      const endAngle = startAngle + sliceAngle;

      ctx.beginPath();
      ctx.arc(centerX, centerY, radius, startAngle, endAngle);
      ctx.strokeStyle = seg.color;
      ctx.lineWidth = thickness;
      ctx.lineCap = 'round';
      ctx.stroke();

      startAngle = endAngle;
    });
  }

  return { drawActivityTrend, drawDonutChart };
})();
