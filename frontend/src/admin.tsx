import React from 'react';
import ReactDOM from 'react-dom/client';
import {
  ChevronLeft,
  ChevronRight,
  FileText,
  Flag,
  LogOut,
  MapPin,
  Search,
  ShieldCheck,
  Sparkles,
  Tags,
  UserPlus,
  Users
} from 'lucide-react';
import './styles.css';
import { readApiData, readError } from './api';

const API_BASE = '/api/admin';
const ADMIN_STORAGE_KEY = 'petshop_admin_session';
const DEFAULT_PAGE_SIZE = 10;

type AdminPermissionCode =
  | 'USER_MODERATE'
  | 'REPORT_REVIEW'
  | 'POST_AUDIT'
  | 'MOMENT_AUDIT'
  | 'CATEGORY_MANAGE'
  | 'REGION_VIEW';

type AdminProfile = {
  id: number;
  username: string;
  displayName?: string;
  role: 'SUPER_ADMIN' | 'ADMIN';
  permissions: AdminPermissionCode[];
  enabled: boolean;
  createdAt?: string;
  lastLoginAt?: string;
};

type AdminSession = { token: string; admin: AdminProfile };
type PermissionOption = { code: AdminPermissionCode; name: string; description: string };
type UserProfile = { id: number; nickname: string; username?: string; city?: string; blacklisted?: boolean };
type ContentReport = { id: number; targetType: 'post' | 'moment'; targetId: number; reason: string; status: string };
type MarketPost = { id: number; title: string; author: string; category: string; city: string; description: string; auditStatus?: string };
type Moment = { id: number; petName: string; author: string; category: string; city: string; content: string; auditStatus?: string };
type Category = { id: number; name: string; description: string; tags: string };
type AdminRegionDistrict = { id: number; name: string; areaCode: string };
type AdminRegionCity = { id: number; name: string; areaCode: string; districtCount: number; districts: AdminRegionDistrict[] };
type AdminRegionProvince = { id: number; name: string; areaCode: string; cityCount: number; districtCount: number; cities: AdminRegionCity[] };
type TabKey = 'accounts' | 'users' | 'reports' | 'posts' | 'moments' | 'categories' | 'regions';
type PageResult<T> = {
  items: T[];
  total: number;
  page: number;
  size: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
};

const TAB_PAGE_SIZES: Record<Exclude<TabKey, 'regions'>, number> = {
  accounts: DEFAULT_PAGE_SIZE,
  users: DEFAULT_PAGE_SIZE,
  reports: DEFAULT_PAGE_SIZE,
  posts: DEFAULT_PAGE_SIZE,
  moments: DEFAULT_PAGE_SIZE,
  categories: DEFAULT_PAGE_SIZE
};

const FALLBACK_PERMISSION_OPTIONS: PermissionOption[] = [
  { code: 'USER_MODERATE', name: '用户治理', description: '查看用户列表、限制与解除限制' },
  { code: 'REPORT_REVIEW', name: '举报处理', description: '查看举报并执行处理动作' },
  { code: 'POST_AUDIT', name: '帖子审核', description: '审核、恢复和下架交易帖' },
  { code: 'MOMENT_AUDIT', name: '动态审核', description: '审核、恢复和下架社区动态' },
  { code: 'CATEGORY_MANAGE', name: '分类管理', description: '维护主站宠物分类' },
  { code: 'REGION_VIEW', name: '地区库查看', description: '查看全国省市区标准地区库' }
];

const EMPTY_PAGE = <T,>(size: number): PageResult<T> => ({
  items: [],
  total: 0,
  page: 1,
  size,
  totalPages: 0,
  hasNext: false,
  hasPrevious: false
});

function getStoredSession() {
  const raw = localStorage.getItem(ADMIN_STORAGE_KEY);
  return raw ? JSON.parse(raw) as AdminSession : null;
}

function saveSession(session: AdminSession) {
  localStorage.setItem(ADMIN_STORAGE_KEY, JSON.stringify(session));
}

function clearSession() {
  localStorage.removeItem(ADMIN_STORAGE_KEY);
}

async function adminFetch(path: string, init: RequestInit = {}) {
  const headers = new Headers(init.headers);
  const token = getStoredSession()?.token;
  if (token && !headers.has('Authorization')) {
    headers.set('Authorization', `Bearer ${token}`);
  }
  if (!headers.has('Content-Type') && init.body && !(init.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json');
  }
  return await fetch(`${API_BASE}${path}`, { ...init, headers });
}

function AdminApp() {
  const [session, setSession] = React.useState<AdminSession | null>(() => getStoredSession());
  const [tab, setTab] = React.useState<TabKey>('accounts');
  const [pages, setPages] = React.useState<Record<Exclude<TabKey, 'regions'>, number>>({
    accounts: 1,
    users: 1,
    reports: 1,
    posts: 1,
    moments: 1,
    categories: 1
  });
  const [notice, setNotice] = React.useState('');
  const [error, setError] = React.useState('');
  const [loading, setLoading] = React.useState(false);

  const [permissionOptions, setPermissionOptions] = React.useState<PermissionOption[]>(FALLBACK_PERMISSION_OPTIONS);
  const [accounts, setAccounts] = React.useState<PageResult<AdminProfile>>(EMPTY_PAGE(TAB_PAGE_SIZES.accounts));
  const [users, setUsers] = React.useState<PageResult<UserProfile>>(EMPTY_PAGE(TAB_PAGE_SIZES.users));
  const [reports, setReports] = React.useState<PageResult<ContentReport>>(EMPTY_PAGE(TAB_PAGE_SIZES.reports));
  const [posts, setPosts] = React.useState<PageResult<MarketPost>>(EMPTY_PAGE(TAB_PAGE_SIZES.posts));
  const [moments, setMoments] = React.useState<PageResult<Moment>>(EMPTY_PAGE(TAB_PAGE_SIZES.moments));
  const [categories, setCategories] = React.useState<PageResult<Category>>(EMPTY_PAGE(TAB_PAGE_SIZES.categories));
  const [regionTree, setRegionTree] = React.useState<AdminRegionProvince[]>([]);

  const [adminForm, setAdminForm] = React.useState({ username: '', password: '', displayName: '', permissions: [] as AdminPermissionCode[] });
  const [categoryForm, setCategoryForm] = React.useState({ name: '', description: '', tags: '' });
  const [permissionDrafts, setPermissionDrafts] = React.useState<Record<number, AdminPermissionCode[]>>({});
  const [regionKeyword, setRegionKeyword] = React.useState('');
  const [selectedProvinceCode, setSelectedProvinceCode] = React.useState('');

  const tabs: Array<{ key: TabKey; label: string; helper: string; icon: React.ReactNode; permission?: AdminPermissionCode; superAdminOnly?: boolean }> = [
    { key: 'accounts', label: '管理员账号', helper: '创建管理员并分配后台权限', icon: <UserPlus size={16} />, superAdminOnly: true },
    { key: 'users', label: '用户管理', helper: '分页处理普通用户治理动作', icon: <Users size={16} />, permission: 'USER_MODERATE' },
    { key: 'reports', label: '举报处理', helper: '按批次处理待审核内容', icon: <Flag size={16} />, permission: 'REPORT_REVIEW' },
    { key: 'posts', label: '帖子审核', helper: '集中审核交易与领养内容', icon: <FileText size={16} />, permission: 'POST_AUDIT' },
    { key: 'moments', label: '动态审核', helper: '查看和处理社区动态内容', icon: <Sparkles size={16} />, permission: 'MOMENT_AUDIT' },
    { key: 'categories', label: '分类管理', helper: '维护主站分类与标签描述', icon: <Tags size={16} />, permission: 'CATEGORY_MANAGE' },
    { key: 'regions', label: '地区库', helper: '按省市区浏览全国标准地区库', icon: <MapPin size={16} />, permission: 'REGION_VIEW' }
  ];

  const visibleTabs = React.useMemo(() => tabs.filter((item) => hasTabAccess(session?.admin, item)), [session]);
  const currentTab = visibleTabs.find((item) => item.key === tab) || visibleTabs[0] || null;
  const activePage = currentTab?.key === 'regions' ? 1 : (currentTab ? pages[currentTab.key] : 1);
  const activeSize = currentTab?.key === 'regions' ? 0 : (currentTab ? TAB_PAGE_SIZES[currentTab.key] : 0);

  const filteredRegionTree = React.useMemo(() => filterRegionTree(regionTree, regionKeyword), [regionTree, regionKeyword]);
  const regionSummary = React.useMemo(() => summarizeRegionTree(filteredRegionTree), [filteredRegionTree]);
  const selectedProvince = React.useMemo(
    () => filteredRegionTree.find((item) => item.areaCode === selectedProvinceCode) || filteredRegionTree[0] || null,
    [filteredRegionTree, selectedProvinceCode]
  );

  const loadPermissionOptions = React.useCallback(async () => {
    if (!session || session.admin.role !== 'SUPER_ADMIN') return;
    const res = await adminFetch('/accounts/permission-options');
    if (!res.ok) return;
    setPermissionOptions(await readApiData<PermissionOption[]>(res));
  }, [session]);

  const loadTab = React.useCallback(async (nextTab: TabKey, nextPage?: number) => {
    if (!getStoredSession()) return;
    setLoading(true);
    setError('');
    try {
      if (nextTab === 'regions') {
        const res = await adminFetch('/regions/tree');
        if (!res.ok) throw new Error(await readError(res));
        setRegionTree(await readApiData<AdminRegionProvince[]>(res));
        return;
      }

      const page = nextPage || pages[nextTab];
      const size = TAB_PAGE_SIZES[nextTab];
      const query = `?page=${page}&size=${size}`;

      if (nextTab === 'accounts') {
        const res = await adminFetch(`/accounts${query}`);
        if (!res.ok) throw new Error(await readError(res));
        const result = await readApiData<PageResult<AdminProfile>>(res);
        setAccounts(result);
        setPermissionDrafts((prev) => {
          const next = { ...prev };
          result.items.forEach((item) => {
            if (!next[item.id]) {
              next[item.id] = item.permissions || [];
            }
          });
          return next;
        });
      } else if (nextTab === 'users') {
        const res = await adminFetch(`/users${query}`);
        if (!res.ok) throw new Error(await readError(res));
        setUsers(await readApiData<PageResult<UserProfile>>(res));
      } else if (nextTab === 'reports') {
        const res = await adminFetch(`/reports${query}`);
        if (!res.ok) throw new Error(await readError(res));
        setReports(await readApiData<PageResult<ContentReport>>(res));
      } else if (nextTab === 'posts') {
        const res = await adminFetch(`/posts${query}`);
        if (!res.ok) throw new Error(await readError(res));
        setPosts(await readApiData<PageResult<MarketPost>>(res));
      } else if (nextTab === 'moments') {
        const res = await adminFetch(`/moments${query}`);
        if (!res.ok) throw new Error(await readError(res));
        setMoments(await readApiData<PageResult<Moment>>(res));
      } else if (nextTab === 'categories') {
        const res = await adminFetch(`/categories${query}`);
        if (!res.ok) throw new Error(await readError(res));
        setCategories(await readApiData<PageResult<Category>>(res));
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : '管理数据加载失败，请重新登录后再试。');
    } finally {
      setLoading(false);
    }
  }, [pages]);

  React.useEffect(() => {
    if (!session) return;
    adminFetch('/auth/me')
      .then((res) => res.ok ? readApiData<AdminProfile>(res) : Promise.reject())
      .then((admin) => {
        const next = { token: session.token, admin };
        setSession(next);
        saveSession(next);
      })
      .catch(() => {
        clearSession();
        setSession(null);
      });
  }, []);

  React.useEffect(() => {
    if (!visibleTabs.length) return;
    if (!currentTab || !visibleTabs.some((item) => item.key === tab)) {
      setTab(visibleTabs[0].key);
    }
  }, [visibleTabs, currentTab, tab]);

  React.useEffect(() => {
    if (!session || !currentTab) return;
    const nextPage = currentTab.key === 'regions' ? undefined : pages[currentTab.key];
    void loadTab(currentTab.key, nextPage);
  }, [session, currentTab, pages, loadTab]);

  React.useEffect(() => {
    if (session?.admin.role === 'SUPER_ADMIN') {
      void loadPermissionOptions();
    }
  }, [session, loadPermissionOptions]);

  React.useEffect(() => {
    if (!filteredRegionTree.length) {
      setSelectedProvinceCode('');
      return;
    }
    if (!filteredRegionTree.some((item) => item.areaCode === selectedProvinceCode)) {
      setSelectedProvinceCode(filteredRegionTree[0].areaCode);
    }
  }, [filteredRegionTree, selectedProvinceCode]);

  async function login(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError('');
    setNotice('');
    const form = new FormData(event.currentTarget);
    const res = await adminFetch('/auth/login', {
      method: 'POST',
      body: JSON.stringify({
        username: String(form.get('username') || '').trim(),
        password: String(form.get('password') || '').trim()
      })
    });
    if (!res.ok) {
      setError(await readError(res));
      return;
    }
    const next = await readApiData<AdminSession>(res);
    setSession(next);
    saveSession(next);
    setNotice('管理员登录成功。');
  }

  async function logout() {
    try {
      await adminFetch('/auth/logout', { method: 'POST' });
    } finally {
      clearSession();
      setSession(null);
    }
  }

  async function createAdmin(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError('');
    const res = await adminFetch('/accounts', { method: 'POST', body: JSON.stringify(adminForm) });
    if (!res.ok) {
      setError(await readError(res));
      return;
    }
    setNotice('管理员账号已创建。');
    setAdminForm({ username: '', password: '', displayName: '', permissions: [] });
    setPages((prev) => ({ ...prev, accounts: 1 }));
    await loadTab('accounts', 1);
  }

  async function updateAdminDisplayName(item: AdminProfile) {
    const displayName = window.prompt('管理员显示名称', item.displayName || item.username);
    if (displayName === null) return;
    setError('');
    const res = await adminFetch(`/accounts/${item.id}/display-name`, {
      method: 'PUT',
      body: JSON.stringify({ displayName })
    });
    if (!res.ok) {
      setError(await readError(res));
      return;
    }
    setNotice('管理员显示名已更新。');
    await loadTab('accounts', pages.accounts);
  }

  async function updateAdminPermissions(id: number) {
    setError('');
    const res = await adminFetch(`/accounts/${id}/permissions`, {
      method: 'PUT',
      body: JSON.stringify({ permissions: permissionDrafts[id] || [] })
    });
    if (!res.ok) {
      setError(await readError(res));
      return;
    }
    setNotice('管理员权限已更新。');
    await loadTab('accounts', pages.accounts);
  }

  async function toggleAdminStatus(item: AdminProfile) {
    setError('');
    const res = await adminFetch(`/accounts/${item.id}/status?enabled=${String(!item.enabled)}`, { method: 'PUT' });
    if (!res.ok) {
      setError(await readError(res));
      return;
    }
    setNotice(item.enabled ? '管理员已停用。' : '管理员已启用。');
    await loadTab('accounts', pages.accounts);
  }

  async function toggleUser(user: UserProfile) {
    setError('');
    const path = user.blacklisted
      ? `/users/${user.id}/unblacklist`
      : `/users/${user.id}/blacklist`;
    const res = await adminFetch(path, {
      method: 'PUT',
      body: user.blacklisted ? undefined : JSON.stringify({ reason: '后台人工限制' })
    });
    if (!res.ok) {
      setError(await readError(res));
      return;
    }
    setNotice(user.blacklisted ? '已解除该用户限制。' : '已限制该用户账号。');
    await loadTab('users', pages.users);
  }

  async function handleReport(id: number, action: string) {
    setError('');
    const res = await adminFetch(`/reports/${id}/handle`, {
      method: 'PUT',
      body: JSON.stringify({ status: '已处理', action, note: '管理员后台处理' })
    });
    if (!res.ok) {
      setError(await readError(res));
      return;
    }
    setNotice('举报已处理。');
    await loadTab('reports', pages.reports);
  }

  async function audit(kind: 'posts' | 'moments', id: number, status: '审核通过' | '已下架') {
    setError('');
    const res = await adminFetch(`/${kind}/${id}/audit?status=${encodeURIComponent(status)}`, { method: 'PUT' });
    if (!res.ok) {
      setError(await readError(res));
      return;
    }
    setNotice('审核状态已更新。');
    await loadTab(kind, pages[kind]);
  }

  async function createCategory(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError('');
    const res = await adminFetch('/categories', { method: 'POST', body: JSON.stringify(categoryForm) });
    if (!res.ok) {
      setError(await readError(res));
      return;
    }
    setNotice('分类已创建。');
    setCategoryForm({ name: '', description: '', tags: '' });
    setPages((prev) => ({ ...prev, categories: 1 }));
    await loadTab('categories', 1);
  }

  async function removeCategory(id: number) {
    setError('');
    const res = await adminFetch(`/categories/${id}`, { method: 'DELETE' });
    if (!res.ok) {
      setError(await readError(res));
      return;
    }
    setNotice('分类已删除。');
    await loadTab('categories', pages.categories);
  }

  if (!session) {
    return (
      <main className="adminStandalone">
        <section className="adminAuthCard">
          <div className="adminHero">
            <ShieldCheck size={28} />
            <div>
              <h1>管理台登录</h1>
              <p>独立后台入口，只处理审核、基础配置和后台账号。</p>
            </div>
          </div>
          <form data-testid="admin-login-form" className="adminAuthForm" onSubmit={login}>
            <label>管理员账号<input data-testid="admin-login-username" name="username" placeholder="输入管理员账号" /></label>
            <label>密码<input data-testid="admin-login-password" name="password" type="password" placeholder="输入密码" /></label>
            {error && <p className="formError">{error}</p>}
            {notice && <p className="formNote">{notice}</p>}
            <button data-testid="admin-login-submit" type="submit" className="submit">登录管理台</button>
          </form>
        </section>
      </main>
    );
  }

  if (!visibleTabs.length || !currentTab) {
    return (
      <main className="adminStandalone adminWorkbench">
        <header className="adminTopbar">
          <div className="adminTopbarTitle">
            <strong>萌宠集市管理台</strong>
            <span>{session.admin.displayName || session.admin.username}</span>
          </div>
          <button type="button" className="ghostButton" onClick={logout}><LogOut size={16} />退出</button>
        </header>
        <section className="adminWorkspace">
          <div className="adminPanel">
            <div className="adminPanelBody">
              <p className="emptyState">当前管理员尚未被授予任何后台权限，请联系超级管理员分配。</p>
            </div>
          </div>
        </section>
      </main>
    );
  }

  return (
    <main className="adminStandalone adminWorkbench">
      <header className="adminTopbar">
        <div className="adminTopbarTitle">
          <strong>萌宠集市管理台</strong>
          <span>{session.admin.displayName || session.admin.username}</span>
        </div>
        <button type="button" className="ghostButton" onClick={logout}><LogOut size={16} />退出</button>
      </header>

      <section className="adminLayout">
        <aside data-testid="admin-tabs" className="adminSidebar">
          {visibleTabs.map((item) => (
            <button
              data-testid={`admin-tab-${item.key}`}
              key={item.key}
              type="button"
              className={currentTab.key === item.key ? 'active' : ''}
              onClick={() => setTab(item.key)}
            >
              {item.icon}
              <span>{item.label}</span>
            </button>
          ))}
        </aside>

        <section className="adminWorkspace">
          <div className="workspaceHeader">
            <div>
              <h2>{currentTab.label}</h2>
              <p>{currentTab.helper}</p>
            </div>
            <div className="workspaceMeta">
              {currentTab.key === 'regions'
                ? (
                  <>
                    <span>{regionSummary.provinces} 省级</span>
                    <span>{regionSummary.cities} 市级</span>
                    <span>{regionSummary.districts} 区县</span>
                  </>
                )
                : (
                  <>
                    <span>第 {activePage} 页</span>
                    <span>每页 {activeSize} 条</span>
                  </>
                )}
            </div>
          </div>

          {loading && <p className="formNote">正在加载管理数据...</p>}
          {error && <p className="formError">{error}</p>}
          {notice && <p className="formNote">{notice}</p>}

          {currentTab.key === 'accounts' && (
            <DataPanel title="管理员账号" total={accounts.total} page={accounts} onPageChange={(next) => setPages((prev) => ({ ...prev, accounts: next }))}>
              <form data-testid="admin-account-create" className="adminInlineForm adminInlineFormWide" onSubmit={createAdmin}>
                <input value={adminForm.username} onChange={(event) => setAdminForm({ ...adminForm, username: event.target.value })} placeholder="管理员用户名" />
                <input value={adminForm.displayName} onChange={(event) => setAdminForm({ ...adminForm, displayName: event.target.value })} placeholder="显示名称" />
                <input value={adminForm.password} onChange={(event) => setAdminForm({ ...adminForm, password: event.target.value })} type="password" placeholder="初始密码" />
                <div className="permissionPicker permissionPickerCompact">
                  {permissionOptions.map((option) => (
                    <label key={option.code} className="permissionCheck">
                      <input
                        type="checkbox"
                        checked={adminForm.permissions.includes(option.code)}
                        onChange={() => setAdminForm((prev) => ({
                          ...prev,
                          permissions: togglePermission(prev.permissions, option.code)
                        }))}
                      />
                      <span>{option.name}</span>
                    </label>
                  ))}
                </div>
                <button type="submit">新建管理员</button>
              </form>

              <div className="adminCardGrid">
                {accounts.items.map((item) => (
                  <article className="adminCard adminCardTall" key={item.id}>
                    <div className="adminCardHeader">
                      <div>
                        <strong>{item.displayName || item.username}</strong>
                        <p>{item.username}</p>
                      </div>
                      <span className={item.role === 'SUPER_ADMIN' ? 'statusOkay' : 'statusNeutral'}>
                        {item.role === 'SUPER_ADMIN' ? '超级管理员' : '普通管理员'}
                      </span>
                    </div>
                    <div className="adminCardMeta">
                      <span>{item.enabled ? '启用中' : '已停用'}</span>
                      <span>{item.lastLoginAt ? `最近登录 ${formatTime(item.lastLoginAt)}` : '尚未登录'}</span>
                    </div>
                    <div className="permissionChipRow">
                      {item.role === 'SUPER_ADMIN'
                        ? <span className="permissionChip permissionChipFull">拥有全部权限</span>
                        : (item.permissions.length
                          ? item.permissions.map((code) => <span key={code} className="permissionChip">{permissionLabel(permissionOptions, code)}</span>)
                          : <span className="permissionChip permissionChipMuted">未分配任何权限</span>)}
                    </div>
                    <div className="adminCardActions">
                      <button type="button" className="ghostSmallButton" onClick={() => updateAdminDisplayName(item)}>修改显示名</button>
                    </div>
                    {item.role !== 'SUPER_ADMIN' && (
                      <>
                        <div className="permissionPicker">
                          {permissionOptions.map((option) => (
                            <label key={option.code} className="permissionCheck">
                              <input
                                type="checkbox"
                                checked={(permissionDrafts[item.id] || item.permissions).includes(option.code)}
                                onChange={() => setPermissionDrafts((prev) => ({
                                  ...prev,
                                  [item.id]: togglePermission(prev[item.id] || item.permissions, option.code)
                                }))}
                              />
                              <span>{option.name}</span>
                            </label>
                          ))}
                        </div>
                        <div className="adminCardActions">
                          <button type="button" onClick={() => updateAdminPermissions(item.id)}>保存权限</button>
                          <button type="button" className="ghostSmallButton" onClick={() => toggleAdminStatus(item)}>
                            {item.enabled ? '停用账号' : '启用账号'}
                          </button>
                        </div>
                      </>
                    )}
                  </article>
                ))}
              </div>
            </DataPanel>
          )}

          {currentTab.key === 'users' && (
            <DataPanel title="普通用户" total={users.total} page={users} onPageChange={(next) => setPages((prev) => ({ ...prev, users: next }))}>
              <div className="adminCardGrid">
                {users.items.map((item) => (
                  <article className="adminCard" key={item.id}>
                    <strong>{item.nickname}</strong>
                    <p>{item.username || '未设置用户名'}{item.city ? ` · ${item.city}` : ''}</p>
                    <div className="adminCardActions">
                      <span className={item.blacklisted ? 'statusDanger' : 'statusOkay'}>{item.blacklisted ? '已限制' : '正常'}</span>
                      <button data-testid={`admin-user-toggle-blacklist-${item.id}`} type="button" onClick={() => toggleUser(item)}>
                        {item.blacklisted ? '解除限制' : '限制账号'}
                      </button>
                    </div>
                  </article>
                ))}
              </div>
            </DataPanel>
          )}

          {currentTab.key === 'reports' && (
            <DataPanel title="举报处理" total={reports.total} page={reports} onPageChange={(next) => setPages((prev) => ({ ...prev, reports: next }))}>
              <div className="adminCardGrid">
                {reports.items.map((item) => (
                  <article className="adminCard" key={item.id}>
                    <strong>{item.targetType === 'post' ? '交易帖' : '动态'} #{item.targetId}</strong>
                    <p>{item.reason}</p>
                    <div className="adminCardActions">
                      <span>{item.status}</span>
                      <div className="inlineActions">
                        <button data-testid={`report-action-remove-${item.id}`} type="button" onClick={() => handleReport(item.id, 'removeTarget')}>下架内容</button>
                        <button data-testid={`report-action-resolve-${item.id}`} type="button" onClick={() => handleReport(item.id, 'removeAndBlockAuthor')}>下架并限制作者</button>
                      </div>
                    </div>
                  </article>
                ))}
              </div>
            </DataPanel>
          )}

          {currentTab.key === 'posts' && (
            <DataPanel title="帖子审核" total={posts.total} page={posts} onPageChange={(next) => setPages((prev) => ({ ...prev, posts: next }))}>
              <div className="adminCardGrid">
                {posts.items.map((item) => (
                  <article className="adminCard" key={item.id}>
                    <strong>{item.title}</strong>
                    <p>{item.author} · {item.category} · {item.city}</p>
                    <p>{item.description}</p>
                    <div className="adminCardActions">
                      <span>{item.auditStatus || '审核通过'}</span>
                      <div className="inlineActions">
                        <button type="button" onClick={() => audit('posts', item.id, '审核通过')}>恢复展示</button>
                        <button type="button" onClick={() => audit('posts', item.id, '已下架')}>下架</button>
                      </div>
                    </div>
                  </article>
                ))}
              </div>
            </DataPanel>
          )}

          {currentTab.key === 'moments' && (
            <DataPanel title="动态审核" total={moments.total} page={moments} onPageChange={(next) => setPages((prev) => ({ ...prev, moments: next }))}>
              <div className="adminCardGrid">
                {moments.items.map((item) => (
                  <article className="adminCard" key={item.id}>
                    <strong>{item.petName}</strong>
                    <p>{item.author} · {item.category} · {item.city}</p>
                    <p>{item.content}</p>
                    <div className="adminCardActions">
                      <span>{item.auditStatus || '审核通过'}</span>
                      <div className="inlineActions">
                        <button type="button" onClick={() => audit('moments', item.id, '审核通过')}>恢复展示</button>
                        <button type="button" onClick={() => audit('moments', item.id, '已下架')}>下架</button>
                      </div>
                    </div>
                  </article>
                ))}
              </div>
            </DataPanel>
          )}

          {currentTab.key === 'categories' && (
            <DataPanel title="分类配置" total={categories.total} page={categories} onPageChange={(next) => setPages((prev) => ({ ...prev, categories: next }))}>
              <form data-testid="admin-category-create" className="adminInlineForm adminInlineFormWide" onSubmit={createCategory}>
                <input value={categoryForm.name} onChange={(event) => setCategoryForm({ ...categoryForm, name: event.target.value })} placeholder="分类名称" />
                <input value={categoryForm.description} onChange={(event) => setCategoryForm({ ...categoryForm, description: event.target.value })} placeholder="分类描述" />
                <input value={categoryForm.tags} onChange={(event) => setCategoryForm({ ...categoryForm, tags: event.target.value })} placeholder="标签，多个用逗号分隔" />
                <button type="submit">新增分类</button>
              </form>
              <div className="adminCardGrid adminCardGridTight">
                {categories.items.map((item) => (
                  <article className="adminCard" key={item.id}>
                    <strong>{item.name}</strong>
                    <p>{item.description || '未填写说明'}</p>
                    <div className="adminCardActions">
                      <span>{item.tags || '无标签'}</span>
                      <button type="button" onClick={() => removeCategory(item.id)}>删除</button>
                    </div>
                  </article>
                ))}
              </div>
            </DataPanel>
          )}

          {currentTab.key === 'regions' && (
            <RegionLibraryPanel
              regionKeyword={regionKeyword}
              onRegionKeyword={setRegionKeyword}
              filteredRegionTree={filteredRegionTree}
              selectedProvince={selectedProvince}
              selectedProvinceCode={selectedProvinceCode}
              onSelectProvince={setSelectedProvinceCode}
            />
          )}
        </section>
      </section>
    </main>
  );
}

function DataPanel<T>({ title, total, page, onPageChange, children }: { title: string; total: number; page: PageResult<T>; onPageChange: (page: number) => void; children: React.ReactNode }) {
  return (
    <div className="adminPanel">
      <div className="adminPanelHeader">
        <div>
          <strong>{title}</strong>
          <span>共 {total} 条</span>
        </div>
      </div>
      <div className="adminPanelBody">{children}</div>
      <PaginationBar page={page.page} totalPages={page.totalPages} total={page.total} onPageChange={onPageChange} />
    </div>
  );
}

function RegionLibraryPanel(props: {
  regionKeyword: string;
  onRegionKeyword: (value: string) => void;
  filteredRegionTree: AdminRegionProvince[];
  selectedProvince: AdminRegionProvince | null;
  selectedProvinceCode: string;
  onSelectProvince: (value: string) => void;
}) {
  return (
    <div className="adminPanel">
      <div className="adminPanelHeader adminPanelHeaderStack">
        <div>
          <strong>全国地区库</strong>
          <span>来源：民政部公开的 2023 年县级以上行政区划代码</span>
        </div>
        <label className="regionSearch">
          <Search size={16} />
          <input value={props.regionKeyword} onChange={(event) => props.onRegionKeyword(event.target.value)} placeholder="筛选省、市、区县或行政区划代码" />
        </label>
      </div>

      <div className="adminPanelBody regionWorkbench">
        <aside className="regionProvinceRail">
          {props.filteredRegionTree.length ? props.filteredRegionTree.map((province) => (
            <button
              key={province.areaCode}
              type="button"
              className={province.areaCode === props.selectedProvinceCode ? 'active' : ''}
              onClick={() => props.onSelectProvince(province.areaCode)}
            >
              <strong>{province.name}</strong>
              <span>{province.cityCount} 市级 · {province.districtCount} 区县</span>
            </button>
          )) : <p className="emptyState">没有匹配到地区结果。</p>}
        </aside>

        <section className="regionDetailPanel">
          {props.selectedProvince ? (
            <>
              <div className="regionDetailHeader">
                <div>
                  <h3>{props.selectedProvince.name}</h3>
                  <p>行政区划代码 {props.selectedProvince.areaCode}</p>
                </div>
                <div className="regionSummaryChips">
                  <span>{props.selectedProvince.cityCount} 个市级节点</span>
                  <span>{props.selectedProvince.districtCount} 个区县节点</span>
                </div>
              </div>
              <div className="regionCityGrid">
                {props.selectedProvince.cities.map((city) => (
                  <article key={city.areaCode} className="regionCityCard">
                    <div className="regionCityHeader">
                      <div>
                        <strong>{city.name}</strong>
                        <span>{city.areaCode}</span>
                      </div>
                      <em>{city.districtCount} 个区县</em>
                    </div>
                    <div className="regionDistrictChipGrid">
                      {city.districts.map((district) => (
                        <span key={district.areaCode} className="regionDistrictChip">{district.name}</span>
                      ))}
                    </div>
                  </article>
                ))}
              </div>
            </>
          ) : <p className="emptyState">请选择左侧省级地区查看下级行政区。</p>}
        </section>
      </div>
    </div>
  );
}

function PaginationBar({ page, totalPages, total, onPageChange }: { page: number; totalPages: number; total: number; onPageChange: (page: number) => void }) {
  if (totalPages <= 1) {
    return <div className="pagerBar pagerBarMuted"><span>共 {total} 条</span></div>;
  }
  return (
    <div className="pagerBar">
      <span>共 {total} 条</span>
      <div className="pagerActions">
        <button type="button" className="pagerButton" disabled={page <= 1} onClick={() => onPageChange(page - 1)}>
          <ChevronLeft size={16} />
          上一页
        </button>
        <span className="pagerStatus">{page} / {totalPages}</span>
        <button type="button" className="pagerButton" disabled={page >= totalPages} onClick={() => onPageChange(page + 1)}>
          下一页
          <ChevronRight size={16} />
        </button>
      </div>
    </div>
  );
}

function hasTabAccess(admin: AdminProfile | null | undefined, tab: { permission?: AdminPermissionCode; superAdminOnly?: boolean }) {
  if (!admin) return false;
  if (admin.role === 'SUPER_ADMIN') return true;
  if (tab.superAdminOnly) return false;
  if (!tab.permission) return false;
  return admin.permissions.includes(tab.permission);
}

function togglePermission(list: AdminPermissionCode[], code: AdminPermissionCode) {
  return list.includes(code) ? list.filter((item) => item !== code) : [...list, code];
}

function permissionLabel(options: PermissionOption[], code: AdminPermissionCode) {
  return options.find((item) => item.code === code)?.name || code;
}

function filterRegionTree(tree: AdminRegionProvince[], keyword: string) {
  const query = keyword.trim().toLowerCase();
  if (!query) return tree;

  return tree
    .map((province) => {
      const provinceMatch = matchesRegionQuery(province.name, province.areaCode, query);
      if (provinceMatch) return province;

      const cities = province.cities
        .map((city) => {
          const cityMatch = matchesRegionQuery(city.name, city.areaCode, query);
          if (cityMatch) return city;

          const districts = city.districts.filter((district) => matchesRegionQuery(district.name, district.areaCode, query));
          if (!districts.length) return null;
          return { ...city, districtCount: districts.length, districts };
        })
        .filter(Boolean) as AdminRegionCity[];

      if (!cities.length) return null;
      return {
        ...province,
        cityCount: cities.length,
        districtCount: cities.reduce((sum, city) => sum + city.districtCount, 0),
        cities
      };
    })
    .filter(Boolean) as AdminRegionProvince[];
}

function matchesRegionQuery(name: string, areaCode: string, query: string) {
  return name.toLowerCase().includes(query) || areaCode.includes(query);
}

function summarizeRegionTree(tree: AdminRegionProvince[]) {
  return tree.reduce((summary, province) => {
    summary.provinces += 1;
    summary.cities += province.cities.length;
    summary.districts += province.cities.reduce((sum, city) => sum + city.districts.length, 0);
    return summary;
  }, { provinces: 0, cities: 0, districts: 0 });
}

function formatTime(value?: string) {
  if (!value) return '';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '';
  return date.toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' });
}

ReactDOM.createRoot(document.getElementById('root') as HTMLElement).render(
  <React.StrictMode>
    <AdminApp />
  </React.StrictMode>
);
