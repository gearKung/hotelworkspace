<template>
  <div class="dashboard-container">
    <header class="dashboard-header">
      <h1>대시보드</h1>
      <p>호텔의 현황을 한눈에 파악하세요.</p>
    </header>

    <div class="summary-grid">
      <div class="card">
        <h3>총 예약 건수</h3>
        <p class="data">1,234건</p>
        <span class="change increase">+12%</span>
      </div>
      <div class="card">
        <h3>오늘 체크인 / 체크아웃</h3>
        <p class="data">15 / 8</p>
        <span class="change"></span>
      </div>
      <div class="card">
        <h3>객실 점유율</h3>
        <p class="data">75%</p>
        <span class="change increase">+5%</span>
      </div>
      <div class="card">
        <h3>오늘 예상 매출</h3>
        <p class="data">₩ 3,450,000</p>
        <span class="change decrease">-3%</span>
      </div>
    </div>

    <div class="main-content-grid">
      <div class="card chart-placeholder">
        <h3>매출 현황 (최근 6개월)</h3>
        <div class="placeholder-content">
          <p>월별 매출 차트가 여기에 표시됩니다.</p>
          </div>
      </div>
      <div class="card recent-activity">
        <h3>최근 예약 활동</h3>
        <ul>
          <li><strong>김민준</strong>님이 <strong>디럭스룸</strong>을 예약했습니다.</li>
          <li><strong>박서연</strong>님이 <strong>스위트룸</strong> 예약을 취소했습니다.</li>
          <li><strong>이도현</strong>님이 <strong>스탠다드룸</strong>에 체크인했습니다.</li>
          <li><strong>최지우</strong>님이 <strong>패밀리룸</strong>을 예약했습니다.</li>
          <li><strong>정하윤</strong>님이 <strong>디럭스룸</strong>에 대한 리뷰를 남겼습니다. (★★★★★)</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'OwnerDashboard',
  // 나중에 차트와 실제 데이터를 연동할 때 아래 주석을 해제하고 사용하세요.
  /*
  components: {
    SalesChart
  },
  data() {
    return {
      // API로부터 받아온 데이터
    };
  },
  created() {
    this.fetchDashboardData();
  },
  methods: {
    fetchDashboardData() {
      // API 호출 로직
    }
  }
  */
}
</script>

<style scoped>
.dashboard-container {
  padding: 40px;
  background-color: #f8f9fa;
  height: 100%;
  box-sizing: border-box;
}

.dashboard-header {
  margin-bottom: 32px;
}

.dashboard-header h1 {
  font-size: 28px;
  font-weight: 800;
  color: #212529;
  margin: 0;
}

.dashboard-header p {
  font-size: 16px;
  color: #6c757d;
  margin-top: 4px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  margin-bottom: 32px;
}

.card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid #e9ecef;
}

.card h3 {
  font-size: 16px;
  font-weight: 700;
  color: #495057;
  margin: 0 0 12px;
}

.card .data {
  font-size: 28px;
  font-weight: 800;
  color: #343a40;
  margin: 0 0 8px;
}

.card .change {
  font-size: 14px;
  font-weight: 600;
}

.change.increase {
  color: #20c997;
}

.change.decrease {
  color: #fa5252;
}

.main-content-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
}

.chart-placeholder .placeholder-content {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 300px;
  background-color: #f1f3f5;
  border-radius: 8px;
  color: #adb5bd;
  font-weight: 600;
}

.recent-activity ul {
  list-style: none;
  padding: 0;
  margin: 0;
  height: 300px;
  overflow-y: auto;
}

.recent-activity li {
  padding: 12px 0;
  border-bottom: 1px solid #f1f3f5;
  color: #495057;
  font-size: 15px;
}

.recent-activity li:last-child {
  border-bottom: none;
}
</style>