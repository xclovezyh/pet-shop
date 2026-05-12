import { expect, test, type Page } from '@playwright/test';

function ok(data: unknown, message = 'ok') {
  return {
    status: 200,
    contentType: 'application/json; charset=utf-8',
    body: JSON.stringify({ success: true, code: 'OK', message, data })
  };
}

function auth(token: string, user: Record<string, unknown>, message: string) {
  return ok({ token, user }, message);
}

function pageOf<T>(items: T[], page = 1, size = items.length || 10) {
  const total = items.length;
  const totalPages = total === 0 ? 0 : Math.ceil(total / size);
  return {
    items,
    total,
    page,
    size,
    totalPages,
    hasNext: false,
    hasPrevious: false
  };
}

function fail(status: number, code: string, message: string) {
  return {
    status,
    contentType: 'application/json; charset=utf-8',
    body: JSON.stringify({ success: false, code, message })
  };
}

async function installApiMocks(page: Page) {
  const superAdmin = {
    id: 1,
    username: 'superadmin',
    displayName: '超级管理员',
    nickname: '超级管理员',
    phone: '13800000000',
    role: 'SUPER_ADMIN',
    enabled: true,
    blacklisted: false,
    city: '上海市 浦东新区'
  };
  const normalUser = {
    id: 2,
    username: 'tester',
    nickname: '普通用户',
    phone: '13900000000',
    role: 'USER',
    blacklisted: false,
    city: '杭州市 西湖区'
  };

  const state = {
    verificationCode: '654321',
    users: [normalUser],
    reports: [
      {
        id: 1,
        targetType: 'post',
        targetId: 101,
        reporter: '普通用户',
        reason: '疑似违规交易',
        status: '待处理',
        createdAt: '2026-05-12T09:00:00'
      }
    ],
    posts: [
      {
        id: 101,
        title: '同城领养布偶猫',
        type: '领养',
        category: '猫咪',
        city: '上海市 浦东新区',
        description: '已完成基础驱虫，寻找稳定家庭。',
        author: '普通用户',
        contact: '站内私信',
        imageUrl: '',
        price: 0,
        auditStatus: '待审核',
        createdAt: '2026-05-12T08:30:00'
      }
    ],
    moments: [
      {
        id: 201,
        author: '普通用户',
        petName: '团子',
        category: '猫咪',
        city: '上海市 浦东新区',
        content: '今天第一次自己开罐头。',
        likes: 5,
        auditStatus: '待审核',
        imageUrl: '',
        createdAt: '2026-05-12T08:45:00'
      }
    ],
    categories: [
      { id: 1, name: '猫咪', description: '适合公寓陪伴。', tags: '新手友好,安静' },
      { id: 2, name: '狗狗', description: '活泼外向。', tags: '陪伴,社交' }
    ],
    regions: [
      {
        id: 1,
        name: '上海市',
        areaCode: '310000',
        cityCount: 1,
        districtCount: 2,
        cities: [
          {
            id: 2,
            name: '上海市',
            areaCode: '310100',
            districtCount: 2,
            districts: [
              { id: 3, name: '浦东新区', areaCode: '310115' },
              { id: 4, name: '徐汇区', areaCode: '310104' }
            ]
          }
        ]
      }
    ],
    passwordLogins: [] as Array<{ account: string; password: string }>,
    smsLogins: [] as Array<{ phone: string; code: string }>,
    registrations: [] as Array<{ username: string; nickname: string; phone: string; password: string; code: string }>,
    adminLogins: [] as Array<{ username: string; password: string }>
  };

  await page.route('**/api/**', async (route) => {
    const url = new URL(route.request().url());
    const { pathname } = url;
    const method = route.request().method();

    if (method === 'GET' && pathname === '/api/categories') return route.fulfill(ok(state.categories));
    if (method === 'GET' && pathname === '/api/pets') return route.fulfill(ok([]));
    if (method === 'GET' && pathname === '/api/posts') return route.fulfill(ok(state.posts));
    if (method === 'GET' && pathname === '/api/moments') return route.fulfill(ok(state.moments));
    if (method === 'GET' && pathname === '/api/reference-data') {
      return route.fulfill(ok({
        regions: [{ name: '上海市', cities: [{ name: '上海市', districts: ['浦东新区'] }] }],
        postTypes: ['售卖', '互换', '领养'],
        petStatuses: ['在售', '可领养'],
        petGenders: ['公', '母'],
        ageRanges: ['幼年', '成年'],
        healthRecords: ['疫苗齐全'],
        personalityTags: ['亲人'],
        serviceTags: ['站内私信']
      }));
    }
    if (method === 'GET' && pathname === '/api/messages') return route.fulfill(ok([]));
    if (method === 'GET' && pathname === '/api/favorites') return route.fulfill(ok([]));
    if (method === 'GET' && pathname === '/api/trade-intents') return route.fulfill(ok([]));
    if (method === 'POST' && pathname === '/api/users/verification-code') {
      const payload = route.request().postDataJSON() as { phone: string };
      return route.fulfill(ok({
        phone: payload.phone,
        code: state.verificationCode,
        expireSeconds: 600,
        message: '开发环境直接返回验证码'
      }, '验证码已生成'));
    }
    if (method === 'POST' && pathname === '/api/users/login/password') {
      const payload = route.request().postDataJSON() as { account: string; password: string };
      state.passwordLogins.push(payload);
      return route.fulfill(auth('token-tester', normalUser, '登录成功'));
    }
    if (method === 'POST' && pathname === '/api/users/login/sms') {
      const payload = route.request().postDataJSON() as { phone: string; code: string };
      state.smsLogins.push(payload);
      return route.fulfill(auth('token-tester', normalUser, '登录成功'));
    }
    if (method === 'POST' && pathname === '/api/users/register') {
      const payload = route.request().postDataJSON() as { username: string; nickname: string; phone: string; password: string; code: string };
      state.registrations.push(payload);
      const createdUser = {
        id: 3,
        username: payload.username,
        nickname: payload.nickname,
        phone: payload.phone,
        role: 'USER',
        blacklisted: false
      };
      state.users.push(createdUser);
      return route.fulfill(auth('token-newuser01', createdUser, '注册成功'));
    }
    if (method === 'POST' && pathname === '/api/users/logout') return route.fulfill(ok(null, '已退出登录'));
    if (method === 'GET' && pathname === '/api/users/me') return route.fulfill(ok(normalUser));

    if (method === 'POST' && pathname === '/api/admin/auth/login') {
      const payload = route.request().postDataJSON() as { username: string; password: string };
      state.adminLogins.push(payload);
      if (payload.username === 'superadmin' && payload.password === 'change-me-admin-password') {
        return route.fulfill(ok({ token: 'admin-token-1', admin: superAdmin }, '管理员登录成功'));
      }
      return route.fulfill(fail(401, 'AUTH_401_001', '账号或密码错误'));
    }
    if (method === 'GET' && pathname === '/api/admin/auth/me') return route.fulfill(ok(superAdmin));
    if (method === 'POST' && pathname === '/api/admin/auth/logout') return route.fulfill(ok(null, '已退出登录'));
    if (method === 'GET' && pathname === '/api/admin/accounts') return route.fulfill(ok(pageOf([superAdmin], 1, 8)));
    if (method === 'GET' && pathname === '/api/admin/users') return route.fulfill(ok(pageOf(state.users, 1, 10)));
    if (method === 'GET' && pathname === '/api/admin/reports') return route.fulfill(ok(pageOf(state.reports, 1, 8)));
    if (method === 'GET' && pathname === '/api/admin/posts') return route.fulfill(ok(pageOf(state.posts, 1, 8)));
    if (method === 'GET' && pathname === '/api/admin/moments') return route.fulfill(ok(pageOf(state.moments, 1, 8)));
    if (method === 'GET' && pathname === '/api/admin/categories') return route.fulfill(ok(pageOf(state.categories, 1, 10)));
    if (method === 'GET' && pathname === '/api/admin/regions/tree') return route.fulfill(ok(state.regions));
    if (method === 'PUT' && pathname === '/api/admin/users/2/blacklist') {
      const user = state.users.find((item) => item.id === 2);
      if (user) user.blacklisted = true;
      return route.fulfill(ok(user, '账号已限制'));
    }
    if (method === 'PUT' && pathname === '/api/admin/reports/1/handle') {
      const report = state.reports.find((item) => item.id === 1);
      if (report) report.status = '已处理';
      return route.fulfill(ok(report, '举报已处理'));
    }

    return route.fulfill(ok([]));
  });

  return state;
}

async function openAccountPage(page: Page) {
  await page.goto('/');
  await page.getByTestId('login-entry').click();
  await expect(page.getByTestId('account-page')).toBeVisible();
}

async function openAdminPage(page: Page) {
  await page.goto('/admin.html');
  await expect(page.getByTestId('admin-login-form')).toBeVisible();
}

test('super admin can login and complete key admin console paths', async ({ page }) => {
  const state = await installApiMocks(page);

  await openAdminPage(page);
  await page.getByTestId('admin-login-username').fill('superadmin');
  await page.getByTestId('admin-login-password').fill('change-me-admin-password');
  await page.getByTestId('admin-login-submit').click();

  await expect(page.getByTestId('admin-tabs')).toBeVisible();
  await expect.poll(() => state.adminLogins.length).toBe(1);

  await page.getByTestId('admin-tab-users').click();
  await expect(page.getByTestId('admin-user-toggle-blacklist-2')).toBeVisible();
  await page.getByTestId('admin-user-toggle-blacklist-2').click();
  await expect(page.getByTestId('admin-user-toggle-blacklist-2')).toContainText('解除限制');

  await page.getByTestId('admin-tab-reports').click();
  await page.getByTestId('report-action-resolve-1').click();
  await expect(page.getByText('举报已处理。', { exact: true })).toBeVisible();

  await page.getByTestId('admin-tab-posts').click();
  await expect(page.getByTestId('admin-tab-posts')).toHaveClass(/active/);

  await page.getByTestId('admin-tab-moments').click();
  await expect(page.getByTestId('admin-tab-moments')).toHaveClass(/active/);

  await page.getByTestId('admin-tab-categories').click();
  await expect(page.getByTestId('admin-category-create')).toBeVisible();

  await page.getByTestId('admin-tab-regions').click();
  await expect(page.getByTestId('admin-tab-regions')).toHaveClass(/active/);
  await expect(page.getByText('全国地区库')).toBeVisible();
});

test('sms login should keep normal user out of admin entry', async ({ page }) => {
  const state = await installApiMocks(page);

  await openAccountPage(page);
  await page.getByTestId('account-mode-sms').click();
  await page.getByTestId('sms-login-phone').fill('13900000000');
  await page.getByTestId('send-verify-code').click();
  await expect(page.getByTestId('sms-login-code')).toHaveValue('654321');
  await page.getByTestId('account-submit').click();

  await expect(page.getByTestId('user-menu-trigger')).toBeVisible();
  await expect.poll(() => state.smsLogins.length).toBe(1);

  await openAdminPage(page);
  await page.getByTestId('admin-login-username').fill('tester');
  await page.getByTestId('admin-login-password').fill('secret123');
  await page.getByTestId('admin-login-submit').click();
  await expect(page.getByText('账号或密码错误', { exact: true })).toBeVisible();
});

test('register flow should create account and auto login', async ({ page }) => {
  const state = await installApiMocks(page);

  await openAccountPage(page);
  await page.getByTestId('account-mode-register').click();
  await page.getByTestId('register-phone').fill('13700000001');
  await page.getByTestId('send-verify-code').click();
  await expect(page.getByTestId('register-code')).toHaveValue('654321');

  await page.getByTestId('register-username').fill('newuser01');
  await page.getByTestId('register-nickname').fill('新用户');
  await page.getByTestId('register-password').fill('secret123');
  await page.getByTestId('account-submit').click();

  await expect(page.getByTestId('user-menu-trigger')).toBeVisible();
  await page.getByTestId('user-menu-trigger').click();
  await expect(page.getByTestId('account-view-profile')).toBeVisible();
  await expect(page.getByTestId('user-menu-trigger')).toContainText('新用户');
  await expect.poll(() => state.registrations.length).toBe(1);
  await expect(state.registrations[0]).toMatchObject({
    username: 'newuser01',
    nickname: '新用户',
    phone: '13700000001',
    code: '654321'
  });
});
