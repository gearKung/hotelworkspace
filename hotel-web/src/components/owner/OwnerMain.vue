<template>
  <div class="owner-page">
    <aside class="sidebar">
      <div class="logo">🏨 Owner Page</div>
      <nav>
        <ul>
          <li :class="{ active: activeMenu === 'OwnerDashboard' }" @click="navigateTo('OwnerDashboard')">대시보드</li>
          <li :class="{ active: activeMenu === 'OwnerRooms' }" @click="navigateTo('OwnerRoom')">객실 관리</li>
          <li :class="{ active: activeMenu === 'OwnerReservations' }" @click="navigateTo('OwnerReservation')">예약 관리</li>
          <li :class="{ active: activeMenu === 'OwnerReviews' }" @click="navigateTo('OwnerReview')">리뷰 관리</li>
        </ul>
      </nav>
      <div class="sidebar-footer">
        <button class="btn-homepage" @click="$router.push('/')">홈페이지 가기</button>
        <button class="btn-logout-sidebar" @click="logoutAndGoHome">로그아웃</button>
      </div>
    </aside>

    <main class="main-content">
      <router-view :user="user" @logout="logoutAndGoHome"></router-view>
    </main>
  </div>
</template>

<script>
export default {
  data() {
    return {
      user: null,
    };
  },
  computed: {
    activeMenu() {
      return this.$route.name;
    }
  },
  methods: {
    checkLoginStatus() {
      const userInfo = localStorage.getItem('user');
      if (userInfo) {
        this.user = JSON.parse(userInfo);
        // 호텔 소유주가 아닐 경우 리디렉션
        if (this.user.role !== 'BUSINESS') {
          alert('접근 권한이 없습니다.');
          this.$router.push('/');
        }
      } else {
        this.$router.push("/login");
      }
    },
    navigateTo(routeName) {
      if (this.$route.name !== routeName) {
        this.$router.push({ name: routeName });
      }
    },
    logoutAndGoHome() {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        alert("로그아웃 되었습니다.");
        this.$router.push('/');
    },
  },
  mounted() {
    this.checkLoginStatus();
  }
}
</script>

<style scoped>
/* 전체 레이아웃 */
.owner-page {
  display: flex;
  height: 100vh;
  width: 100vw;
  margin: 0;
  background: #f3f4f6;
  font-family: 'Pretendard', sans-serif;
}

/* 사이드바 */
.sidebar {
  width: 240px; /* 너비 조정 */
  background: #1f2937; /* 다크 네이비 색상 */
  color: #e5e7eb;
  padding: 24px 16px;
  box-sizing: border-box;
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  transition: width 0.2s;
}

.sidebar .logo {
  font-weight: 800;
  font-size: 22px;
  margin-bottom: 30px;
  text-align: center;
  color: #fff;
}

.sidebar nav {
  flex-grow: 1;
}

.sidebar ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.sidebar li {
  padding: 14px 20px;
  cursor: pointer;
  border-radius: 8px;
  margin: 8px 0;
  font-weight: 600;
  color: #d1d5db;
  transition: background-color .2s, color .2s;
}

.sidebar li.active, .sidebar li:hover {
  background: #4b5563;
  color: #fff;
}

/* 사이드바 하단 */
.sidebar-footer {
  padding: 16px;
  border-top: 1px solid #374151;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.sidebar-footer button {
  width: 100%;
  padding: 12px;
  border: none;
  border-radius: 8px;
  color: white;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  transition: background-color 0.2s;
}

.btn-homepage {
  background-color: #4f46e5;
}

.btn-homepage:hover {
  background-color: #4338ca;
}

.btn-logout-sidebar {
  background-color: #ef4444;
}

.btn-logout-sidebar:hover {
  background-color: #dc2626;
}

/* 메인 콘텐츠 영역 */
.main-content {
  margin-left: 240px;
  width: calc(100% - 240px);
  height: 100vh;
  padding: 0;
  box-sizing: border-box;
  overflow-y: auto;
}
</style>