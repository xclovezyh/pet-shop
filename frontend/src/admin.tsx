import React from 'react';
import ReactDOM from 'react-dom/client';
import {
  ChevronLeft,
  ChevronRight,
  FileText,
  Flag,
  LogOut,
  MapPin,
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

type AdminProfile = {
  id: number;
  username: string;
  displayName?: string;
  enabled: boolean;
  createdAt?: string;
  lastLoginAt?: string;
};

type AdminSession = { token: string; admin: AdminProfile };
type UserProfile = { id: number; nickname: string; username?: string; city?: string; blacklisted?: boolean };
type ContentReport = { id: number; targetType: 'post' | 'moment'; targetId: number; reason: string; status: string };
type MarketPost = { id: number; title: string; author: string; category: string; city: string; description: string; auditStatus?: string };
type Moment = { id: number; petName: string; author: string; category: string; city: string; content: string; auditStatus?: string };
type Category = { id: number; name: string; description: string; tags: string };
type RegionArea = { id: number; name: string; level: 'province' | 'city' | 'district'; parentId?: number; sortOrder?: number };
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

const TAB_PAGE_SIZES: Record<TabKey, number> = {
  accounts: 6,
  users: 8,
  reports: 8,
  posts: 6,
  moments: 6,
  categories: 8,
  regions: 8
};

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
  const [pages, setPages] = React.useState<Record<TabKey, number>>({
    accounts: 1,
    users: 1,
    reports: 1,
    posts: 1,
    moments: 1,
    categories: 1,
    regions: 1
  });
  const [notice, setNotice] = React.useState('');
  const [error, setError] = React.useState('');
  const [loading, setLoading] = React.useState(false);

  const [accounts, setAccounts] = React.useState<PageResult<AdminProfile>>(EMPTY_PAGE(TAB_PAGE_SIZES.accounts));
  const [users, setUsers] = React.useState<PageResult<UserProfile>>(EMPTY_PAGE(TAB_PAGE_SIZES.users));
  const [reports, setReports] = React.useState<PageResult<ContentReport>>(EMPTY_PAGE(TAB_PAGE_SIZES.reports));
  const [posts, setPosts] = React.useState<PageResult<MarketPost>>(EMPTY_PAGE(TAB_PAGE_SIZES.posts));
  const [moments, setMoments] = React.useState<PageResult<Moment>>(EMPTY_PAGE(TAB_PAGE_SIZES.moments));
  const [categories, setCategories] = React.useState<PageResult<Category>>(EMPTY_PAGE(TAB_PAGE_SIZES.categories));
  const [regions, setRegions] = React.useState<PageResult<RegionArea>>(EMPTY_PAGE(TAB_PAGE_SIZES.regions));

  const [adminForm, setAdminForm] = React.useState({ username: '', password: '', displayName: '' });
  const [categoryForm, setCategoryForm] = React.useState({ name: '', description: '', tags: '' });
  const [regionForm, setRegionForm] = React.useState({ name: '', level: 'province' as RegionArea['level'], parentId: '' });

  const page = pages[tab];
  const size = TAB_PAGE_SIZES[tab];

  const tabs: Array<{ key: TabKey; label: string; helper: string; icon: React.ReactNode }> = [
    { key: 'accounts', label: '管理员账号', helper: '独立维护后台账号与状态', icon: <UserPlus size={16} /> },
    { key: 'users', label: '用户管理', helper: '分页处理普通用户治理动作', icon: <Users size={16} /> },
    { key: 'reports', label: '举报处理', helper: '按批次处理待审核内容', icon: <Flag size={16} /> },
    { key: 'posts', label: '帖子审核', helper: '避免长列表堆满一个页面', icon: <FileText size={16} /> },
    { key: 'moments', label: '日常审核', helper: '单页聚焦当前待看的内容', icon: <Sparkles size={16} /> },
    { key: 'categories', label: '分类管理', helper: '结构化维护分类基础库', icon: <Tags size={16} /> },
    { key: 'regions', label: '地区库', helper: '分页维护行政区层级', icon: <MapPin size={16} /> }
  ];

  const currentTab = tabs.find((item) => item.key === tab)!;

  const loadTab = React.useCallback(async (nextTab: TabKey, nextPage: number) => {
    if (!getStoredSession()) return;
    setLoading(true);
    setError('');
    try {
      const query = `?page=${nextPage}&size=${TAB_PAGE_SIZES[nextTab]}`;
      if (nextTab === 'accounts') {
        const res = await adminFetch(`/accounts${query}`);
        if (!res.ok) throw new Error(await readError(res));
        setAccounts(await readApiData<PageResult<AdminProfile>>(res));
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
      } else if (nextTab === 'regions') {
        const res = await adminFetch(`/regions${query}`);
        if (!res.ok) throw new Error(await readError(res));
        setRegions(await readApiData<PageResult<RegionArea>>(res));
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : '管理数据加载失败，请重新登录。');
    } finally {
      setLoading(false);
    }
  }, []);

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
    if (!session) return;
    void loadTab(tab, page);
  }, [session, tab, page, loadTab]);

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
    setTab('accounts');
    setPages((prev) => ({ ...prev, accounts: 1 }));
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
    setAdminForm({ username: '', password: '', displayName: '' });
    setPages((prev) => ({ ...prev, accounts: 1 }));
    await loadTab('accounts', 1);
  }

  async function toggleUser(user: UserProfile) {
    setError('');
    const path = user.blacklisted ? `/users/${user.id}/unblacklist` : `/users/${user.id}/blacklist?reason=${encodeURIComponent('后台手动限制')}`;
    const res = await adminFetch(path, { method: 'PUT' });
    if (!res.ok) {
      setError(await readError(res));
      return;
    }
    setNotice(user.blacklisted ? '已解除用户限制。' : '已限制该用户。');
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

  async function createRegion(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError('');
    const res = await adminFetch('/regions', {
      method: 'POST',
      body: JSON.stringify({
        name: regionForm.name,
        level: regionForm.level,
        parentId: regionForm.parentId ? Number(regionForm.parentId) : undefined
      })
    });
    if (!res.ok) {
      setError(await readError(res));
      return;
    }
    setNotice('地区已创建。');
    setRegionForm({ name: '', level: 'province', parentId: '' });
    setPages((prev) => ({ ...prev, regions: 1 }));
    await loadTab('regions', 1);
  }

  async function removeRegion(id: number) {
    setError('');
    const res = await adminFetch(`/regions/${id}`, { method: 'DELETE' });
    if (!res.ok) {
      setError(await readError(res));
      return;
    }
    setNotice('地区已删除。');
    await loadTab('regions', pages.regions);
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
          {tabs.map((item) => (
            <button
              data-testid={`admin-tab-${item.key}`}
              key={item.key}
              type="button"
              className={tab === item.key ? 'active' : ''}
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
              <span>第 {page} 页</span>
              <span>每页 {size} 条</span>
            </div>
          </div>

          {loading && <p className="formNote">正在加载管理数据...</p>}
          {error && <p className="formError">{error}</p>}
          {notice && <p className="formNote">{notice}</p>}

          {tab === 'accounts' && (
            <DataPanel title="后台账号列表" total={accounts.total} page={accounts} onPageChange={(next) => setPages((prev) => ({ ...prev, accounts: next }))}>
              <form data-testid="admin-account-create" className="adminInlineForm adminInlineFormWide" onSubmit={createAdmin}>
                <input value={adminForm.username} onChange={(event) => setAdminForm({ ...adminForm, username: event.target.value })} placeholder="管理员用户名" />
                <input value={adminForm.displayName} onChange={(event) => setAdminForm({ ...adminForm, displayName: event.target.value })} placeholder="显示名称" />
                <input value={adminForm.password} onChange={(event) => setAdminForm({ ...adminForm, password: event.target.value })} type="password" placeholder="初始密码" />
                <button type="submit">新建管理员</button>
              </form>
              <div className="adminCardGrid adminCardGridTight">
                {accounts.items.map((item) => (
                  <article className="adminCard" key={item.id}>
                    <strong>{item.displayName || item.username}</strong>
                    <p>{item.username}</p>
                    <div className="adminCardMeta">
                      <span>{item.enabled ? '启用中' : '已停用'}</span>
                      <span>{item.lastLoginAt ? `最近登录 ${formatTime(item.lastLoginAt)}` : '尚未登录'}</span>
                    </div>
                  </article>
                ))}
              </div>
            </DataPanel>
          )}

          {tab === 'users' && (
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

          {tab === 'reports' && (
            <DataPanel title="举报处理" total={reports.total} page={reports} onPageChange={(next) => setPages((prev) => ({ ...prev, reports: next }))}>
              <div className="adminCardGrid">
                {reports.items.map((item) => (
                  <article className="adminCard" key={item.id}>
                    <strong>{item.targetType === 'post' ? '交易帖' : '日常'} #{item.targetId}</strong>
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

          {tab === 'posts' && (
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

          {tab === 'moments' && (
            <DataPanel title="日常审核" total={moments.total} page={moments} onPageChange={(next) => setPages((prev) => ({ ...prev, moments: next }))}>
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

          {tab === 'categories' && (
            <DataPanel title="分类配置" total={categories.total} page={categories} onPageChange={(next) => setPages((prev) => ({ ...prev, categories: next }))}>
              <form data-testid="admin-category-create" className="adminInlineForm adminInlineFormWide" onSubmit={createCategory}>
                <input value={categoryForm.name} onChange={(event) => setCategoryForm({ ...categoryForm, name: event.target.value })} placeholder="分类名称" />
                <input value={categoryForm.tags} onChange={(event) => setCategoryForm({ ...categoryForm, tags: event.target.value })} placeholder="标签，逗号分隔" />
                <input value={categoryForm.description} onChange={(event) => setCategoryForm({ ...categoryForm, description: event.target.value })} placeholder="分类说明" />
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

          {tab === 'regions' && (
            <DataPanel title="地区配置" total={regions.total} page={regions} onPageChange={(next) => setPages((prev) => ({ ...prev, regions: next }))}>
              <form className="adminInlineForm adminInlineFormWide" onSubmit={createRegion}>
                <input value={regionForm.name} onChange={(event) => setRegionForm({ ...regionForm, name: event.target.value })} placeholder="地区名称" />
                <select value={regionForm.level} onChange={(event) => setRegionForm({ ...regionForm, level: event.target.value as RegionArea['level'] })}>
                  <option value="province">省份</option>
                  <option value="city">城市</option>
                  <option value="district">区县</option>
                </select>
                <input value={regionForm.parentId} onChange={(event) => setRegionForm({ ...regionForm, parentId: event.target.value })} placeholder="父级 ID，省级可留空" />
                <button type="submit">新增地区</button>
              </form>
              <div className="adminCardGrid adminCardGridTight">
                {regions.items.map((item) => (
                  <article className="adminCard" key={item.id}>
                    <strong>{item.name}</strong>
                    <p>{regionLevelLabel(item.level)}{item.parentId ? ` · 父级 ${item.parentId}` : ''}</p>
                    <div className="adminCardActions">
                      <span>排序 {item.sortOrder || 0}</span>
                      <button type="button" onClick={() => removeRegion(item.id)}>删除</button>
                    </div>
                  </article>
                ))}
              </div>
            </DataPanel>
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

function regionLevelLabel(level: RegionArea['level']) {
  if (level === 'province') return '省份';
  if (level === 'city') return '城市';
  return '区县';
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
