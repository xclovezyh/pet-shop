import React from 'react';
import ReactDOM from 'react-dom/client';
import { FileText, Flag, LogOut, MapPin, ShieldCheck, Sparkles, Tags, UserPlus, Users } from 'lucide-react';
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
  const [notice, setNotice] = React.useState('');
  const [error, setError] = React.useState('');
  const [loading, setLoading] = React.useState(false);
  const [accounts, setAccounts] = React.useState<AdminProfile[]>([]);
  const [users, setUsers] = React.useState<UserProfile[]>([]);
  const [reports, setReports] = React.useState<ContentReport[]>([]);
  const [posts, setPosts] = React.useState<MarketPost[]>([]);
  const [moments, setMoments] = React.useState<Moment[]>([]);
  const [categories, setCategories] = React.useState<Category[]>([]);
  const [regions, setRegions] = React.useState<RegionArea[]>([]);
  const [adminForm, setAdminForm] = React.useState({ username: '', password: '', displayName: '' });
  const [categoryForm, setCategoryForm] = React.useState({ name: '', description: '', tags: '' });
  const [regionForm, setRegionForm] = React.useState({ name: '', level: 'province' as RegionArea['level'], parentId: '' });

  const loadAll = React.useCallback(async () => {
    if (!getStoredSession()) return;
    setLoading(true);
    setError('');
    try {
      const results = await Promise.all([
        adminFetch('/accounts'),
        adminFetch('/users'),
        adminFetch('/reports'),
        adminFetch('/posts'),
        adminFetch('/moments'),
        adminFetch('/categories'),
        adminFetch('/regions')
      ]);
      if (results.some((res) => !res.ok)) {
        throw new Error('load');
      }
      const [accountsData, usersData, reportsData, postsData, momentsData, categoriesData, regionsData] = await Promise.all([
        readApiData<AdminProfile[]>(results[0]),
        readApiData<UserProfile[]>(results[1]),
        readApiData<ContentReport[]>(results[2]),
        readApiData<MarketPost[]>(results[3]),
        readApiData<Moment[]>(results[4]),
        readApiData<Category[]>(results[5]),
        readApiData<RegionArea[]>(results[6])
      ]);
      setAccounts(accountsData);
      setUsers(usersData);
      setReports(reportsData);
      setPosts(postsData);
      setMoments(momentsData);
      setCategories(categoriesData);
      setRegions(regionsData);
    } catch {
      setError('管理数据加载失败，请重新登录。');
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
        loadAll();
      })
      .catch(() => {
        clearSession();
        setSession(null);
      });
  }, []);

  async function login(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError('');
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
    await loadAll();
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
    const res = await adminFetch('/accounts', { method: 'POST', body: JSON.stringify(adminForm) });
    if (!res.ok) {
      setError(await readError(res));
      return;
    }
    setNotice('管理员账号已创建。');
    setAdminForm({ username: '', password: '', displayName: '' });
    await loadAll();
  }

  async function toggleUser(user: UserProfile) {
    const path = user.blacklisted ? `/users/${user.id}/unblacklist` : `/users/${user.id}/blacklist?reason=${encodeURIComponent('后台手动限制')}`;
    const res = await adminFetch(path, { method: 'PUT' });
    if (!res.ok) {
      setError(await readError(res));
      return;
    }
    setNotice(user.blacklisted ? '已解除用户限制。' : '已限制该用户。');
    await loadAll();
  }

  async function handleReport(id: number, action: string) {
    const res = await adminFetch(`/reports/${id}/handle`, {
      method: 'PUT',
      body: JSON.stringify({ status: '已处理', action, note: '管理员后台处理' })
    });
    if (!res.ok) {
      setError(await readError(res));
      return;
    }
    setNotice('举报已处理。');
    await loadAll();
  }

  async function audit(kind: 'posts' | 'moments', id: number, status: '审核通过' | '已下架') {
    const res = await adminFetch(`/${kind}/${id}/audit?status=${encodeURIComponent(status)}`, { method: 'PUT' });
    if (!res.ok) {
      setError(await readError(res));
      return;
    }
    setNotice('审核状态已更新。');
    await loadAll();
  }

  async function createCategory(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const res = await adminFetch('/categories', { method: 'POST', body: JSON.stringify(categoryForm) });
    if (!res.ok) {
      setError(await readError(res));
      return;
    }
    setNotice('分类已创建。');
    setCategoryForm({ name: '', description: '', tags: '' });
    await loadAll();
  }

  async function removeCategory(id: number) {
    const res = await adminFetch(`/categories/${id}`, { method: 'DELETE' });
    if (!res.ok) {
      setError(await readError(res));
      return;
    }
    setNotice('分类已删除。');
    await loadAll();
  }

  async function createRegion(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
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
    await loadAll();
  }

  async function removeRegion(id: number) {
    const res = await adminFetch(`/regions/${id}`, { method: 'DELETE' });
    if (!res.ok) {
      setError(await readError(res));
      return;
    }
    setNotice('地区已删除。');
    await loadAll();
  }

  if (!session) {
    return (
      <main className="adminStandalone">
        <section className="adminAuthCard">
          <div className="adminHero">
            <ShieldCheck size={26} />
            <div>
              <h1>管理台登录</h1>
              <p>这里是独立管理员后台，不与普通用户站点混用。</p>
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

  const tabs: Array<{ key: TabKey; label: string; icon: React.ReactNode }> = [
    { key: 'accounts', label: '管理员账号', icon: <UserPlus size={16} /> },
    { key: 'users', label: '用户管理', icon: <Users size={16} /> },
    { key: 'reports', label: '举报处理', icon: <Flag size={16} /> },
    { key: 'posts', label: '帖子审核', icon: <FileText size={16} /> },
    { key: 'moments', label: '日常审核', icon: <Sparkles size={16} /> },
    { key: 'categories', label: '分类管理', icon: <Tags size={16} /> },
    { key: 'regions', label: '地区库', icon: <MapPin size={16} /> }
  ];

  return (
    <main className="adminStandalone">
      <header className="adminTopbar">
        <div>
          <strong>萌宠集市管理台</strong>
          <span>{session.admin.displayName || session.admin.username}</span>
        </div>
        <button type="button" className="ghostButton" onClick={logout}><LogOut size={16} />退出</button>
      </header>

      <section className="adminLayout">
        <aside data-testid="admin-tabs" className="adminSidebar">
          {tabs.map((item) => <button data-testid={`admin-tab-${item.key}`} key={item.key} type="button" className={tab === item.key ? 'active' : ''} onClick={() => setTab(item.key)}>{item.icon}{item.label}</button>)}
        </aside>
        <section className="adminWorkspace">
          {loading && <p className="formNote">正在加载管理数据...</p>}
          {error && <p className="formError">{error}</p>}
          {notice && <p className="formNote">{notice}</p>}

          {tab === 'accounts' && <AdminAccounts accounts={accounts} adminForm={adminForm} setAdminForm={setAdminForm} onSubmit={createAdmin} />}
          {tab === 'users' && <UserList users={users} onToggle={toggleUser} />}
          {tab === 'reports' && <ReportList reports={reports} onHandle={handleReport} />}
          {tab === 'posts' && <PostList posts={posts} onAudit={(id, status) => audit('posts', id, status)} />}
          {tab === 'moments' && <MomentList moments={moments} onAudit={(id, status) => audit('moments', id, status)} />}
          {tab === 'categories' && <CategoryList categories={categories} categoryForm={categoryForm} setCategoryForm={setCategoryForm} onSubmit={createCategory} onDelete={removeCategory} />}
          {tab === 'regions' && <RegionList regions={regions} regionForm={regionForm} setRegionForm={setRegionForm} onSubmit={createRegion} onDelete={removeRegion} />}
        </section>
      </section>
    </main>
  );
}

function AdminAccounts({ accounts, adminForm, setAdminForm, onSubmit }: { accounts: AdminProfile[]; adminForm: { username: string; password: string; displayName: string }; setAdminForm: (value: { username: string; password: string; displayName: string }) => void; onSubmit: (event: React.FormEvent<HTMLFormElement>) => void }) {
  return (
    <div className="adminSection">
      <h2>管理员账号</h2>
      <form data-testid="admin-account-create" className="adminInlineForm" onSubmit={onSubmit}>
        <input value={adminForm.username} onChange={(event) => setAdminForm({ ...adminForm, username: event.target.value })} placeholder="管理员用户名" />
        <input value={adminForm.displayName} onChange={(event) => setAdminForm({ ...adminForm, displayName: event.target.value })} placeholder="显示名称" />
        <input value={adminForm.password} onChange={(event) => setAdminForm({ ...adminForm, password: event.target.value })} type="password" placeholder="初始密码" />
        <button type="submit">新建管理员</button>
      </form>
      <div className="adminList">
        {accounts.map((item) => <article className="adminCard" key={item.id}><strong>{item.displayName || item.username}</strong><p>{item.username}</p><span>{item.enabled ? '启用中' : '已停用'}</span></article>)}
      </div>
    </div>
  );
}

function UserList({ users, onToggle }: { users: UserProfile[]; onToggle: (user: UserProfile) => void }) {
  return <div className="adminSection"><h2>普通用户</h2><div className="adminList">{users.map((item) => <article className="adminCard" key={item.id}><strong>{item.nickname}</strong><p>{item.username || '未设置用户名'} {item.city ? `· ${item.city}` : ''}</p><div className="adminCardActions"><span>{item.blacklisted ? '已限制' : '正常'}</span><button data-testid={`admin-user-toggle-blacklist-${item.id}`} type="button" onClick={() => onToggle(item)}>{item.blacklisted ? '解除限制' : '限制账号'}</button></div></article>)}</div></div>;
}

function ReportList({ reports, onHandle }: { reports: ContentReport[]; onHandle: (id: number, action: string) => void }) {
  return <div className="adminSection"><h2>举报处理</h2><div className="adminList">{reports.map((item) => <article className="adminCard" key={item.id}><strong>{item.targetType} #{item.targetId}</strong><p>{item.reason}</p><div className="adminCardActions"><span>{item.status}</span><button data-testid={`report-action-remove-${item.id}`} type="button" onClick={() => onHandle(item.id, 'removeTarget')}>下架内容</button><button data-testid={`report-action-resolve-${item.id}`} type="button" onClick={() => onHandle(item.id, 'removeAndBlockAuthor')}>下架并限制作者</button></div></article>)}</div></div>;
}

function PostList({ posts, onAudit }: { posts: MarketPost[]; onAudit: (id: number, status: '审核通过' | '已下架') => void }) {
  return <div className="adminSection"><h2>帖子审核</h2><div className="adminList">{posts.map((item) => <article className="adminCard" key={item.id}><strong>{item.title}</strong><p>{item.author} · {item.category} · {item.city}</p><p>{item.description}</p><div className="adminCardActions"><span>{item.auditStatus || '审核通过'}</span><button type="button" onClick={() => onAudit(item.id, '审核通过')}>恢复展示</button><button type="button" onClick={() => onAudit(item.id, '已下架')}>下架</button></div></article>)}</div></div>;
}

function MomentList({ moments, onAudit }: { moments: Moment[]; onAudit: (id: number, status: '审核通过' | '已下架') => void }) {
  return <div className="adminSection"><h2>日常审核</h2><div className="adminList">{moments.map((item) => <article className="adminCard" key={item.id}><strong>{item.petName}</strong><p>{item.author} · {item.category} · {item.city}</p><p>{item.content}</p><div className="adminCardActions"><span>{item.auditStatus || '审核通过'}</span><button type="button" onClick={() => onAudit(item.id, '审核通过')}>恢复展示</button><button type="button" onClick={() => onAudit(item.id, '已下架')}>下架</button></div></article>)}</div></div>;
}

function CategoryList({ categories, categoryForm, setCategoryForm, onSubmit, onDelete }: { categories: Category[]; categoryForm: { name: string; description: string; tags: string }; setCategoryForm: (value: { name: string; description: string; tags: string }) => void; onSubmit: (event: React.FormEvent<HTMLFormElement>) => void; onDelete: (id: number) => void }) {
  return <div className="adminSection"><h2>分类管理</h2><form data-testid="admin-category-create" className="adminInlineForm" onSubmit={onSubmit}><input value={categoryForm.name} onChange={(event) => setCategoryForm({ ...categoryForm, name: event.target.value })} placeholder="分类名称" /><input value={categoryForm.tags} onChange={(event) => setCategoryForm({ ...categoryForm, tags: event.target.value })} placeholder="标签" /><input value={categoryForm.description} onChange={(event) => setCategoryForm({ ...categoryForm, description: event.target.value })} placeholder="分类说明" /><button type="submit">新增分类</button></form><div className="adminList">{categories.map((item) => <article className="adminCard" key={item.id}><strong>{item.name}</strong><p>{item.description || '未填写说明'}</p><div className="adminCardActions"><span>{item.tags || '无标签'}</span><button type="button" onClick={() => onDelete(item.id)}>删除</button></div></article>)}</div></div>;
}

function RegionList({ regions, regionForm, setRegionForm, onSubmit, onDelete }: { regions: RegionArea[]; regionForm: { name: string; level: RegionArea['level']; parentId: string }; setRegionForm: (value: { name: string; level: RegionArea['level']; parentId: string }) => void; onSubmit: (event: React.FormEvent<HTMLFormElement>) => void; onDelete: (id: number) => void }) {
  return <div className="adminSection"><h2>地区库</h2><form className="adminInlineForm" onSubmit={onSubmit}><input value={regionForm.name} onChange={(event) => setRegionForm({ ...regionForm, name: event.target.value })} placeholder="地区名称" /><select value={regionForm.level} onChange={(event) => setRegionForm({ ...regionForm, level: event.target.value as RegionArea['level'] })}><option value="province">省份</option><option value="city">城市</option><option value="district">区县</option></select><input value={regionForm.parentId} onChange={(event) => setRegionForm({ ...regionForm, parentId: event.target.value })} placeholder="父级 ID（省份可留空）" /><button type="submit">新增地区</button></form><div className="adminList">{regions.map((item) => <article className="adminCard" key={item.id}><strong>{item.name}</strong><p>{item.level} {item.parentId ? `· 父级 ${item.parentId}` : ''}</p><div className="adminCardActions"><span>排序 {item.sortOrder || 0}</span><button type="button" onClick={() => onDelete(item.id)}>删除</button></div></article>)}</div></div>;
}

ReactDOM.createRoot(document.getElementById('root') as HTMLElement).render(
  <React.StrictMode>
    <AdminApp />
  </React.StrictMode>
);
