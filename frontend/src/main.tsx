import React from 'react';
import ReactDOM from 'react-dom/client';
import {
  Camera,
  CheckCircle2,
  Clock3,
  Flag,
  Heart,
  Lock,
  LogIn,
  MapPin,
  MessageCircle,
  PawPrint,
  Pencil,
  Plus,
  Search,
  ShieldCheck,
  Store,
  Tags,
  Trash2,
  User,
  X
} from 'lucide-react';
import './styles.css';

const API_BASE = '/api';
const CONTACT_VALUE = '站内私信';
const phonePattern = /(?:\+?86[-\s]?)?1[3-9]\d{9}/;
const offsiteContactPattern = /(?:微信|vx|wechat|qq|企鹅|扣扣)[:：\s-]*[a-z0-9_-]{4,}|[1-9]\d{5,11}/i;
const sensitiveWords = ['虐待', '毒药', '赌博', '色情', '诈骗', '保护动物', '野生动物', '线下交易', '加微信', '加qq'];
const MAX_IMAGE_SIZE = 5 * 1024 * 1024;
const allowedImageTypes = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];
const tradeStatuses = ['在售', '已预约', '已成交', '已关闭'];
const tradeTypeConfigs: Record<string, { title: string; priceLabel: string; description: string; fields: Array<{ name: string; label: string; placeholder: string; required?: boolean }> }> = {
  售卖: {
    title: '售卖宠物或用品',
    priceLabel: '售价',
    description: '写清品相、健康状态、包含物品和同城交付方式。',
    fields: [{ name: 'handover', label: '交付方式', placeholder: '例如同城自提、送到地铁站、仅限附近社区' }]
  },
  互换: {
    title: '寻找互换伙伴',
    priceLabel: '补差价',
    description: '说明你能提供什么、希望换到什么，以及可接受的互换范围。',
    fields: [{ name: 'swapExpectation', label: '互换期望', placeholder: '例如同城短期寄养互助、用品置换、同品类交换', required: true }]
  },
  领养: {
    title: '发布领养信息',
    priceLabel: '领养费用',
    description: '建议说明领养条件、回访意愿和宠物基础情况。',
    fields: [{ name: 'adoptionRequirement', label: '领养要求', placeholder: '例如有稳定住所、接受回访、不笼养', required: true }]
  },
  闲置: {
    title: '转让闲置用品',
    priceLabel: '转让价',
    description: '补充成色、购入时间、适用宠物和是否支持自提。',
    fields: [{ name: 'idleCondition', label: '闲置成色', placeholder: '例如九成新，猫爬架使用 2 个月', required: true }]
  },
  寄养: {
    title: '发布寄养需求',
    priceLabel: '预算',
    description: '说明寄养周期、宠物习惯、喂养注意事项和期望照护方式。',
    fields: [{ name: 'fosterDuration', label: '寄养周期', placeholder: '例如 5 月 20 日至 5 月 27 日', required: true }]
  }
};

type AppUser = { id: number; nickname: string; username?: string; phone?: string; role?: string; blacklisted?: boolean; blacklistReason?: string };
type UserProfile = AppUser & { avatarUrl?: string; bio?: string; city?: string };
type Category = { id: number; name: string; description: string; tags: string };
type Pet = {
  id: number;
  name: string;
  category: string;
  breed: string;
  age: string;
  city: string;
  status: string;
  price: number;
  imageUrl: string;
  imageUrls?: string;
  healthInfo: string;
  healthRecords?: string;
  personality: string;
  ownerName?: string;
  gender?: string;
  ageRange?: string;
  vaccinated?: boolean;
  dewormed?: boolean;
  neutered?: boolean;
  careNotes?: string;
  createdAt?: string;
};
type MarketPost = {
  id: number;
  title: string;
  type: string;
  category: string;
  city: string;
  description: string;
  author: string;
  contact: string;
  imageUrl: string;
  imageUrls?: string;
  price?: number;
  status?: string;
  auditStatus?: string;
  createdAt?: string;
};
type Moment = {
  id: number;
  author: string;
  petName: string;
  category?: string;
  city?: string;
  content: string;
  likes: number;
  auditStatus?: string;
  imageUrl: string;
  imageUrls?: string;
  createdAt?: string;
};
type MomentComment = { id: number; momentId: number; author: string; content: string; createdAt: string };
type PostFavorite = { id: number; userNickname: string; postId: number; createdAt: string; post?: MarketPost };
type TradeIntent = { id: number; postId: number; postTitle: string; requester: string; owner: string; message: string; status: string; createdAt: string; updatedAt: string; post?: MarketPost };
type ContentReport = { id: number; targetType: 'post' | 'moment'; targetId: number; reporter: string; reason: string; status: string; createdAt: string; handledBy?: string; handledAt?: string; handleNote?: string };
type RegionArea = { id: number; name: string; level: 'province' | 'city' | 'district'; parentId?: number; sortOrder?: number };
type Region = { name: string; cities: Array<{ name: string; districts: string[] }> };
type PageKey = 'home' | 'guide' | 'market' | 'moments' | 'mine' | 'profile' | 'messages' | 'favorites' | 'admin' | 'account';
type AdminTab = 'reports' | 'users' | 'posts' | 'moments' | 'categories' | 'regions';
type MessageItem = { id: number; threadId: number; sender: string; content: string; readByRecipient: boolean; createdAt: string };
type MessageThread = { id: number; postId: number; peer: string; postTitle: string; unreadCount: number; messages: MessageItem[] };
type ReferenceData = {
  regions: Region[];
  postTypes: string[];
  petStatuses: string[];
  petGenders: string[];
  ageRanges: string[];
  healthRecords: string[];
  personalityTags: string[];
  serviceTags: string[];
};

const fallbackRegions: Region[] = [
  { name: '上海市', cities: [{ name: '上海市', districts: ['浦东新区', '徐汇区', '静安区', '闵行区'] }] },
  { name: '浙江省', cities: [{ name: '杭州市', districts: ['西湖区', '拱墅区', '滨江区', '余杭区'] }] },
  { name: '江苏省', cities: [{ name: '南京市', districts: ['玄武区', '秦淮区', '建邺区'] }] }
];

const fallbackReferenceData: ReferenceData = {
  regions: fallbackRegions,
  postTypes: ['互换', '售卖', '领养', '闲置', '求助', '寄养', '寻宠'],
  petStatuses: ['在售', '可领养', '可互换'],
  petGenders: ['公', '母', '未知'],
  ageRanges: ['幼年', '青年', '成年', '老年'],
  healthRecords: ['疫苗齐全', '已驱虫', '已绝育', '体检正常'],
  personalityTags: ['亲人', '安静', '活泼', '胆小', '独立'],
  serviceTags: ['站内私信', '同城自提', '线下看宠']
};

const demoCategories: Category[] = [
  { id: 1, name: '猫咪', description: '温顺亲人，适合公寓和家庭陪伴。', tags: '新手友好,安静,陪伴型' },
  { id: 2, name: '狗狗', description: '活泼忠诚，需要规律运动和训练。', tags: '互动强,需要遛弯,家庭型' },
  { id: 3, name: '小宠', description: '仓鼠、兔子、龙猫等，占地小但需要细心照顾。', tags: '空间小,易观察,轻陪伴' },
  { id: 4, name: '用品', description: '食品、玩具、猫爬架、牵引绳等宠物用品。', tags: '闲置交易,日常消耗,养宠装备' }
];

const demoPets: Pet[] = [
  { id: 1, name: '团子', category: '猫咪', breed: '英短银渐层', age: '8个月', city: '上海市 上海市 浦东新区', status: '在售', price: 1800, imageUrl: '', healthInfo: '疫苗齐全，已驱虫', personality: '安静亲人，喜欢陪睡' },
  { id: 2, name: '可乐', category: '狗狗', breed: '柯基', age: '1岁', city: '浙江省 杭州市 西湖区', status: '可互换', price: 0, imageUrl: '', healthInfo: '体检正常，精力充沛', personality: '活泼黏人，会坐下握手' }
];

const demoPosts: MarketPost[] = [
  { id: 1, title: '想给柯基找同城互换寄养伙伴', type: '互换', category: '狗狗', city: '浙江省 杭州市 西湖区', description: '工作日偶尔出差，希望找同城稳定互助家庭。', author: '林小满', contact: CONTACT_VALUE, imageUrl: '', price: 0 }
];

const demoMoments: Moment[] = [
  { id: 1, author: '林小满', petName: '团子', category: '猫咪', content: '今天第一次学会自己开零食罐。', likes: 28, imageUrl: '' }
];

function useApi<T>(path: string, fallback: T) {
  const [data, setData] = React.useState<T>(fallback);
  const [loading, setLoading] = React.useState(true);
  const load = React.useCallback(() => {
    setLoading(true);
    fetch(`${API_BASE}${path}`)
      .then((res) => res.json())
      .then(setData)
      .catch(() => setData(fallback))
      .finally(() => setLoading(false));
  }, [path, fallback]);
  React.useEffect(load, [load]);
  return { data, loading, reload: load };
}

function App() {
  const categories = useApi<Category[]>('/categories', demoCategories);
  const pets = useApi<Pet[]>('/pets', demoPets);
  const posts = useApi<MarketPost[]>('/posts', demoPosts);
  const moments = useApi<Moment[]>('/moments', demoMoments);
  const referenceData = useApi<ReferenceData>('/reference-data', fallbackReferenceData);
  const [searchQuery, setSearchQuery] = React.useState('');
  const [categoryFilter, setCategoryFilter] = React.useState('全部');
  const [cityFilter, setCityFilter] = React.useState('全部');
  const [typeFilter, setTypeFilter] = React.useState('全部');
  const [minPrice, setMinPrice] = React.useState('');
  const [maxPrice, setMaxPrice] = React.useState('');
  const [sortMode, setSortMode] = React.useState<'latest' | 'oldest'>('latest');
  const [page, setPage] = React.useState<PageKey>('home');
  const [currentUser, setCurrentUser] = React.useState<UserProfile | null>(() => {
    const raw = localStorage.getItem('petshop_user');
    return raw ? JSON.parse(raw) : null;
  });
  const [detail, setDetail] = React.useState<{ type: 'post' | 'moment' | 'pet'; item: MarketPost | Moment | Pet } | null>(null);
  const [editing, setEditing] = React.useState<{ type: 'post' | 'moment'; item: MarketPost | Moment } | null>(null);
  const [threads, setThreads] = React.useState<MessageThread[]>([]);
  const [favoritePosts, setFavoritePosts] = React.useState<MarketPost[]>([]);
  const [sentIntents, setSentIntents] = React.useState<TradeIntent[]>([]);
  const [receivedIntents, setReceivedIntents] = React.useState<TradeIntent[]>([]);
  const availableCategories = categories.data.map((category) => category.name);
  const availableCities = cityOptions(referenceData.data.regions.length ? referenceData.data.regions : fallbackRegions);
  const filteredCategories = categories.data.filter((category) => matchesText(searchQuery, [category.name, category.description, category.tags]));
  const filteredPets = sortByTime(pets.data
    .filter((pet) => matchesCategory(categoryFilter, pet.category))
    .filter((pet) => matchesCity(cityFilter, pet.city))
    .filter((pet) => matchesPrice(minPrice, maxPrice, pet.price))
    .filter((pet) => matchesText(searchQuery, [pet.name, pet.category, pet.breed, pet.city, pet.status, pet.healthInfo, pet.personality])), sortMode);
  const filteredPosts = sortByTime(posts.data
    .filter((post) => matchesCategory(categoryFilter, post.category))
    .filter((post) => matchesCity(cityFilter, post.city))
    .filter((post) => matchesType(typeFilter, post.type))
    .filter((post) => matchesPrice(minPrice, maxPrice, post.price))
    .filter((post) => matchesText(searchQuery, [post.title, post.type, post.category, post.city, post.description, post.author])), sortMode);
  const filteredMoments = sortByTime(moments.data
    .filter((moment) => matchesCategory(categoryFilter, moment.category))
    .filter((moment) => matchesCity(cityFilter, moment.city))
    .filter((moment) => matchesText(searchQuery, [moment.author, moment.petName, moment.category, moment.city, moment.content])), sortMode);

  function handleLogin(user: AppUser) {
    localStorage.setItem('petshop_user', JSON.stringify(user));
    setCurrentUser(user);
  }

  function handleProfileSaved(user: UserProfile) {
    localStorage.setItem('petshop_user', JSON.stringify(user));
    setCurrentUser(user);
  }

  function logout() {
    localStorage.removeItem('petshop_user');
    setCurrentUser(null);
  }

  React.useEffect(() => {
    if (!currentUser) return;
    fetch(`${API_BASE}/users/exists?nickname=${encodeURIComponent(currentUser.nickname)}`)
      .then((res) => res.ok ? res.json() : Promise.reject())
      .then((user) => {
        if (user.blacklisted) {
          alert(user.blacklistReason || '账号已被限制，暂不能继续操作。');
          logout();
          return;
        }
        localStorage.setItem('petshop_user', JSON.stringify(user));
        setCurrentUser(user);
      })
      .catch(() => logout());
  }, []);

  const loadThreads = React.useCallback(() => {
    if (!currentUser) {
      setThreads([]);
      return;
    }
    fetch(`${API_BASE}/messages?user=${encodeURIComponent(currentUser.nickname)}`)
      .then((res) => res.ok ? res.json() : Promise.reject())
      .then(setThreads)
      .catch(() => setThreads([]));
  }, [currentUser]);

  React.useEffect(loadThreads, [loadThreads]);

  const loadFavorites = React.useCallback(() => {
    if (!currentUser) {
      setFavoritePosts([]);
      return;
    }
    fetch(`${API_BASE}/favorites?user=${encodeURIComponent(currentUser.nickname)}`)
      .then((res) => res.ok ? res.json() : Promise.reject())
      .then((items: PostFavorite[]) => setFavoritePosts(items.map((item) => item.post).filter(Boolean) as MarketPost[]))
      .catch(() => setFavoritePosts([]));
  }, [currentUser]);

  React.useEffect(loadFavorites, [loadFavorites]);

  const loadTradeIntents = React.useCallback(() => {
    if (!currentUser) {
      setSentIntents([]);
      setReceivedIntents([]);
      return;
    }
    const user = encodeURIComponent(currentUser.nickname);
    Promise.all([
      fetch(`${API_BASE}/trade-intents?user=${user}&role=requester`).then((res) => res.ok ? res.json() : []),
      fetch(`${API_BASE}/trade-intents?user=${user}&role=owner`).then((res) => res.ok ? res.json() : [])
    ])
      .then(([sent, received]) => {
        setSentIntents(sent);
        setReceivedIntents(received);
      })
      .catch(() => {
        setSentIntents([]);
        setReceivedIntents([]);
      });
  }, [currentUser]);

  React.useEffect(loadTradeIntents, [loadTradeIntents]);

  const reloadFeeds = () => {
    posts.reload();
    moments.reload();
  };

  async function startMessage(post: MarketPost) {
    if (!currentUser) {
      alert('请先登录后再私信发布者。');
      return;
    }
    const res = await fetch(`${API_BASE}/messages/start`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ postId: post.id, sender: currentUser.nickname, content: `你好，我想了解「${post.title}」。` })
    });
    if (!res.ok) {
      alert(await readError(res));
      return;
    }
    setDetail(null);
    setPage('messages');
    loadThreads();
  }

  async function toggleFavorite(post: MarketPost) {
    if (!currentUser) {
      alert('请先登录后再收藏帖子。');
      return;
    }
    const exists = favoritePosts.some((item) => item.id === post.id);
    const res = await fetch(`${API_BASE}/favorites${exists ? `/${post.id}?user=${encodeURIComponent(currentUser.nickname)}` : ''}`, {
      method: exists ? 'DELETE' : 'POST',
      headers: exists ? undefined : { 'Content-Type': 'application/json' },
      body: exists ? undefined : JSON.stringify({ userNickname: currentUser.nickname, postId: post.id })
    });
    if (!res.ok) {
      alert(await readError(res));
      return;
    }
    loadFavorites();
  }

  async function reportContent(type: 'post' | 'moment', id: number) {
    if (!currentUser) {
      alert('请先登录后再举报内容。');
      return;
    }
    const reason = window.prompt('请填写举报原因');
    const text = (reason || '').trim();
    if (!text) return;
    if (hasUnsafeContent(text)) {
      alert('举报原因不能包含手机号、微信号、QQ号或敏感词。');
      return;
    }
    const res = await fetch(`${API_BASE}/reports`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ targetType: type, targetId: id, reporter: currentUser.nickname, reason: text })
    });
    alert(res.ok ? '举报已提交，等待处理。' : await readError(res));
  }

  async function submitTradeIntent(post: MarketPost, message: string) {
    if (!currentUser) {
      alert('请先登录后再提交交易意向。');
      return false;
    }
    const res = await fetch(`${API_BASE}/trade-intents`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ postId: post.id, requester: currentUser.nickname, message })
    });
    if (!res.ok) {
      alert(await readError(res));
      return false;
    }
    loadTradeIntents();
    return true;
  }

  async function updateTradeIntent(intent: TradeIntent, status: string) {
    if (!currentUser) return;
    const res = await fetch(`${API_BASE}/trade-intents/${intent.id}/status?user=${encodeURIComponent(currentUser.nickname)}&status=${encodeURIComponent(status)}`, { method: 'PUT' });
    if (!res.ok) {
      alert(await readError(res));
      return;
    }
    loadTradeIntents();
  }

  const favoriteIds = new Set(favoritePosts.map((post) => post.id));
  const unreadCount = threads.reduce((count, thread) => count + (thread.unreadCount || 0), 0);

  return (
    <main>
      <header className="topbar">
        <div className="brand"><PawPrint /><span>萌宠集市</span></div>
        <nav>
          <button type="button" className={page === 'home' ? 'active' : ''} onClick={() => setPage('home')}>首页</button>
          <button type="button" className={page === 'guide' ? 'active' : ''} onClick={() => setPage('guide')}>百科</button>
          <button type="button" className={page === 'market' ? 'active' : ''} onClick={() => setPage('market')}>市场</button>
          <button type="button" className={page === 'moments' ? 'active' : ''} onClick={() => setPage('moments')}>日常</button>
        </nav>
        <LoginBox currentUser={currentUser} unreadCount={unreadCount} onAccount={() => setPage('account')} onMine={() => setPage('mine')} onProfile={() => setPage('profile')} onMessages={() => setPage('messages')} onAdmin={() => setPage('admin')} onLogout={logout} />
      </header>

      {page === 'home' && (
        <HomePage
          categories={categories.data}
          pets={pets.data}
          posts={posts.data}
          moments={moments.data}
          currentUser={currentUser}
          favoriteIds={favoriteIds}
          onNavigate={setPage}
          onOpenPet={(pet) => setDetail({ type: 'pet', item: pet })}
          onOpenPost={(post) => setDetail({ type: 'post', item: post })}
          onOpenMoment={(moment) => setDetail({ type: 'moment', item: moment })}
          onToggleFavorite={toggleFavorite}
        />
      )}
      {page === 'guide' && <GuidePage categoryLoading={categories.loading} petLoading={pets.loading} categories={filteredCategories} pets={filteredPets} onOpenPet={(pet) => setDetail({ type: 'pet', item: pet })} />}
      {page === 'market' && (
        <MarketPage
          searchQuery={searchQuery}
          categoryFilter={categoryFilter}
          cityFilter={cityFilter}
          typeFilter={typeFilter}
          minPrice={minPrice}
          maxPrice={maxPrice}
          sortMode={sortMode}
          loading={posts.loading}
          availableCategories={availableCategories}
          availableCities={availableCities}
          postTypes={referenceData.data.postTypes}
          posts={filteredPosts}
          categories={categories.data}
          referenceData={referenceData.data}
          currentUser={currentUser}
          onSearch={setSearchQuery}
          onCategory={setCategoryFilter}
          onCity={setCityFilter}
          onType={setTypeFilter}
          onMinPrice={setMinPrice}
          onMaxPrice={setMaxPrice}
          onSort={setSortMode}
          onOpenPost={(post) => setDetail({ type: 'post', item: post })}
          favoriteIds={favoriteIds}
          onToggleFavorite={toggleFavorite}
          onPublished={reloadFeeds}
        />
      )}
      {page === 'moments' && <MomentsPage loading={moments.loading} moments={filteredMoments} onOpen={(moment) => setDetail({ type: 'moment', item: moment })} />}
      {page === 'mine' && <MinePage currentUser={currentUser} posts={posts.data} moments={moments.data} sentIntents={sentIntents} receivedIntents={receivedIntents} onOpen={setDetail} onEdit={setEditing} onChanged={reloadFeeds} onIntentStatus={updateTradeIntent} />}
      {page === 'profile' && <ProfilePage currentUser={currentUser} referenceData={referenceData.data} posts={posts.data} favoriteCount={favoritePosts.length} onSaved={handleProfileSaved} onFavorites={() => setPage('favorites')} onMessages={() => setPage('messages')} />}
      {page === 'messages' && <MessagesPage currentUser={currentUser} threads={threads} onThreadsChange={setThreads} onReload={loadThreads} />}
      {page === 'favorites' && <FavoritesPage currentUser={currentUser} posts={favoritePosts} favoriteIds={favoriteIds} onOpen={(post) => setDetail({ type: 'post', item: post })} onToggleFavorite={toggleFavorite} />}
      {page === 'admin' && <AdminPage currentUser={currentUser} onOpen={setDetail} onChanged={reloadFeeds} />}
      {page === 'account' && <AccountPage currentUser={currentUser} onLogin={handleLogin} onProfile={() => setPage('profile')} />}

      {detail && <DetailModal detail={detail} currentUser={currentUser} favoriteIds={favoriteIds} sentIntents={sentIntents} onFavorite={toggleFavorite} onReport={reportContent} onMessage={startMessage} onTradeIntent={submitTradeIntent} onMomentChanged={reloadFeeds} onClose={() => setDetail(null)} />}
      {editing && <EditModal detail={editing} categories={categories.data} referenceData={referenceData.data} currentUser={currentUser} onClose={() => setEditing(null)} onSaved={() => { setEditing(null); reloadFeeds(); }} />}
    </main>
  );
}

function LoginBox({ currentUser, unreadCount, onAccount, onMine, onProfile, onMessages, onAdmin, onLogout }: { currentUser: UserProfile | null; unreadCount: number; onAccount: () => void; onMine: () => void; onProfile: () => void; onMessages: () => void; onAdmin: () => void; onLogout: () => void }) {
  const [open, setOpen] = React.useState(false);
  const menuRef = React.useRef<HTMLDivElement | null>(null);

  React.useEffect(() => {
    if (!open) return;
    function closeOnOutside(event: MouseEvent) {
      if (!menuRef.current?.contains(event.target as Node)) {
        setOpen(false);
      }
    }
    function closeOnScroll() {
      setOpen(false);
    }
    function closeOnEscape(event: KeyboardEvent) {
      if (event.key === 'Escape') setOpen(false);
    }
    document.addEventListener('mousedown', closeOnOutside);
    window.addEventListener('scroll', closeOnScroll, true);
    window.addEventListener('keydown', closeOnEscape);
    return () => {
      document.removeEventListener('mousedown', closeOnOutside);
      window.removeEventListener('scroll', closeOnScroll, true);
      window.removeEventListener('keydown', closeOnEscape);
    };
  }, [open]);

  if (currentUser) {
    return (
      <div className="userMenu" ref={menuRef}>
        <button type="button" className="userMenuTrigger" aria-expanded={open} onClick={() => setOpen(!open)}>
          <span className="avatarMini">{currentUser.nickname.slice(0, 1)}</span>
          <span>{currentUser.nickname}</span>
          {unreadCount > 0 && <em>{unreadCount}</em>}
        </button>
        {open && <div className="userMenuPanel">
          <button type="button" onClick={() => { setOpen(false); onMine(); }}>我的发布</button>
          <button type="button" onClick={() => { setOpen(false); onProfile(); }}>个人主页</button>
          <button type="button" onClick={() => { setOpen(false); onMessages(); }}>站内私信{unreadCount > 0 ? ` ${unreadCount}` : ''}</button>
          {isSuperAdmin(currentUser) && <button type="button" onClick={() => { setOpen(false); onAdmin(); }}>管理后台</button>}
          <button type="button" className="logoutItem" onClick={() => { setOpen(false); onLogout(); }}>退出登录</button>
        </div>}
      </div>
    );
  }

  return (
    <button type="button" className="loginBox loginEntry" onClick={onAccount}>
      <LogIn size={16} />
      <span>登录 / 注册</span>
    </button>
  );
}

function AccountPage({ currentUser, onLogin, onProfile }: { currentUser: UserProfile | null; onLogin: (user: UserProfile) => void; onProfile: () => void }) {
  const [mode, setMode] = React.useState<'password' | 'sms' | 'register'>('password');
  const [account, setAccount] = React.useState('');
  const [username, setUsername] = React.useState('');
  const [nickname, setNickname] = React.useState('');
  const [phone, setPhone] = React.useState('');
  const [password, setPassword] = React.useState('');
  const [code, setCode] = React.useState('');
  const [adminCode, setAdminCode] = React.useState('');
  const [notice, setNotice] = React.useState('');
  const [error, setError] = React.useState('');
  const [busy, setBusy] = React.useState(false);
  const [sending, setSending] = React.useState(false);

  async function sendCode() {
    const mobile = phone.trim();
    setError('');
    setNotice('');
    if (!phonePattern.test(mobile)) return setError('请输入正确的手机号');
    setSending(true);
    try {
      const res = await fetch(`${API_BASE}/users/verification-code`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ phone: mobile })
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.message || await readError(res));
      setCode(data.code || '');
      setNotice(data.code ? `开发验证码：${data.code}` : '验证码已发送，请查看短信。');
    } catch (err) {
      setError(err instanceof Error ? err.message : '验证码发送失败');
    } finally {
      setSending(false);
    }
  }

  async function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError('');
    setNotice('');
    setBusy(true);
    try {
      const endpoint = mode === 'register' ? '/users/register' : mode === 'sms' ? '/users/login/sms' : '/users/login/password';
      const payload = mode === 'register'
        ? { username, nickname, phone, password, code, adminCode }
        : mode === 'sms'
          ? { phone, code, adminCode }
          : { account, password, adminCode };
      const res = await fetch(`${API_BASE}${endpoint}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      if (!res.ok) throw new Error(await readError(res));
      const user = await res.json();
      onLogin(user);
      setNotice(mode === 'register' ? '注册成功，已自动登录。' : '登录成功。');
    } catch (err) {
      setError(err instanceof Error ? err.message : '操作失败，请稍后重试。');
    } finally {
      setBusy(false);
    }
  }

  if (currentUser) {
    return (
      <section className="page section accountPage">
        <SectionTitle icon={<User />} title="账号中心" helper="管理登录身份、个人资料和站内沟通入口" />
        <div className="accountShell signed">
          <div className="accountSummary">
            <span className="avatarLarge">{currentUser.nickname.slice(0, 1)}</span>
            <div>
              <h3>{currentUser.nickname}</h3>
              <p>{currentUser.username ? `用户名：${currentUser.username}` : '已登录账号'}</p>
              {currentUser.phone && <p>手机号：{maskPhone(currentUser.phone)}</p>}
              {isSuperAdmin(currentUser) && <strong>超级管理员</strong>}
            </div>
          </div>
          <button type="button" onClick={onProfile}>查看个人主页</button>
        </div>
      </section>
    );
  }

  return (
    <section className="page section accountPage">
      <SectionTitle icon={<LogIn />} title="登录 / 注册" helper="使用手机号验证码或用户名密码进入账号，发布、收藏、私信都绑定到你的账号" />
      <div className="accountShell">
        <div className="accountTabs">
          <button type="button" className={mode === 'password' ? 'active' : ''} onClick={() => setMode('password')}>密码登录</button>
          <button type="button" className={mode === 'sms' ? 'active' : ''} onClick={() => setMode('sms')}>验证码登录</button>
          <button type="button" className={mode === 'register' ? 'active' : ''} onClick={() => setMode('register')}>注册账号</button>
        </div>
        <form className="accountForm" onSubmit={submit}>
          {mode === 'password' && (
            <>
              <label>用户名或手机号<input value={account} onChange={(event) => setAccount(event.target.value)} placeholder="输入用户名或手机号" /></label>
              <label>密码<input value={password} onChange={(event) => setPassword(event.target.value)} type="password" placeholder="输入密码" /></label>
            </>
          )}
          {mode === 'sms' && (
            <>
              <label>手机号<input value={phone} onChange={(event) => setPhone(event.target.value)} placeholder="输入手机号" /></label>
              <div className="codeRow">
                <label>验证码<input value={code} onChange={(event) => setCode(event.target.value)} placeholder="6 位验证码" /></label>
                <button type="button" onClick={sendCode} disabled={sending}>{sending ? '发送中' : '获取验证码'}</button>
              </div>
            </>
          )}
          {mode === 'register' && (
            <>
              <label>用户名<input value={username} onChange={(event) => setUsername(event.target.value)} placeholder="字母开头，4-20 位" /></label>
              <label>展示昵称<input value={nickname} onChange={(event) => setNickname(event.target.value)} placeholder="发布和私信中显示的名字" /></label>
              <label>手机号<input value={phone} onChange={(event) => setPhone(event.target.value)} placeholder="用于验证码注册" /></label>
              <div className="codeRow">
                <label>验证码<input value={code} onChange={(event) => setCode(event.target.value)} placeholder="6 位验证码" /></label>
                <button type="button" onClick={sendCode} disabled={sending}>{sending ? '发送中' : '获取验证码'}</button>
              </div>
              <label>密码<input value={password} onChange={(event) => setPassword(event.target.value)} type="password" placeholder="至少 6 位" /></label>
            </>
          )}
          <label>管理员口令<input value={adminCode} onChange={(event) => setAdminCode(event.target.value)} type="password" placeholder="普通用户无需填写" /></label>
          {error && <p className="formError">{error}</p>}
          {notice && <p className="formNote">{notice}</p>}
          <button type="submit" disabled={busy}>{busy ? '处理中...' : mode === 'register' ? '注册并登录' : '登录'}</button>
        </form>
      </div>
    </section>
  );
}

function HomePage(props: {
  categories: Category[];
  pets: Pet[];
  posts: MarketPost[];
  moments: Moment[];
  currentUser: UserProfile | null;
  favoriteIds: Set<number>;
  onNavigate: (page: PageKey) => void;
  onOpenPet: (pet: Pet) => void;
  onOpenPost: (post: MarketPost) => void;
  onOpenMoment: (moment: Moment) => void;
  onToggleFavorite: (post: MarketPost) => void;
}) {
  return (
    <>
      <section className="hero">
        <div className="heroCopy">
          <p className="eyebrow">宠物百科 · 站内沟通 · 同城社区</p>
          <h1>把宠物展示、交易互换和日常分享放在一个清爽空间里</h1>
          <p className="lead">登录后可发布和管理自己的内容。交易沟通固定使用站内私信，前后端都会拦截手机号。</p>
          <div className="heroActions">
            <button type="button" onClick={() => props.onNavigate('market')}>进入市场</button>
            <button type="button" onClick={() => props.onNavigate('profile')}>完善主页</button>
          </div>
        </div>
        <div className="heroPanel">
          <Metric value={props.categories.length} label="分类库" />
          <Metric value={props.pets.length} label="展示宠物" />
          <Metric value={props.posts.length} label="交易帖子" />
        </div>
      </section>
      <section className="section dashboard">
        <SectionTitle icon={<Store />} title="今日概览" helper="从这里快速进入各个业务页面" />
        <div className="dashboardGrid">
          <button type="button" onClick={() => props.onNavigate('guide')}><Tags /><strong>宠物百科</strong><span>{props.categories.length} 个分类 · {props.pets.length} 个宠物</span></button>
          <button type="button" onClick={() => props.onNavigate('market')}><Store /><strong>售卖互换</strong><span>{props.posts.length} 条帖子</span></button>
          <button type="button" onClick={() => props.onNavigate('moments')}><Camera /><strong>日常分享</strong><span>{props.moments.length} 条日常</span></button>
        </div>
      </section>
      <section className="section previewSplit">
        <div>
          <SectionTitle icon={<Plus />} title="最新交易" helper="市场页可筛选城市、分类、类型和发布时间" />
          <PostList posts={props.posts.slice(0, 3)} currentUser={props.currentUser} favoriteIds={props.favoriteIds} onOpen={props.onOpenPost} onToggleFavorite={props.onToggleFavorite} />
        </div>
        <div>
          <SectionTitle icon={<Camera />} title="最新日常" helper="社区页集中查看用户分享" />
          <MomentList moments={props.moments.slice(0, 2)} onOpen={props.onOpenMoment} />
        </div>
      </section>
      <section className="section">
        <SectionTitle icon={<PawPrint />} title="宠物推荐" helper="更多宠物信息在宠物页查看" />
        <div className="petGrid">
          {props.pets.slice(0, 4).map((pet) => <PetCard key={pet.id} pet={pet} onOpen={props.onOpenPet} />)}
          {props.pets.length === 0 && <EmptyState title="还没有宠物展示" helper="后续可在管理端补充宠物资料。" />}
        </div>
      </section>
    </>
  );
}

function CategoriesPage({ loading, categories }: { loading: boolean; categories: Category[] }) {
  return (
    <section className="page section">
      <SectionTitle icon={<Tags />} title="宠物分类库" helper="发布内容时必须从平台分类库中选择" />
        <div className="categoryGrid">
        {loading && <LoadingState label="正在加载分类库" />}
        {categories.map((category) => (
          <article className="category" key={category.id}>
            <h3>{category.name}</h3>
            <p>{category.description}</p>
            <div className="chips">{category.tags.split(',').map((tag) => <span key={tag}>{tag}</span>)}</div>
          </article>
        ))}
        {!loading && categories.length === 0 && <EmptyState title="没有匹配的分类" helper="换个关键词或清空筛选条件再试。" />}
      </div>
    </section>
  );
}

function GuidePage({ categoryLoading, petLoading, categories, pets, onOpenPet }: { categoryLoading: boolean; petLoading: boolean; categories: Category[]; pets: Pet[]; onOpenPet: (pet: Pet) => void }) {
  return (
    <section className="page section guidePage">
      <SectionTitle icon={<Tags />} title="宠物百科" helper="分类库和宠物资料合并展示，发布内容时可参考平台分类" />
      <div className="guideLayout">
        <div>
          <h3>分类库</h3>
          <div className="categoryGrid compactGrid">
            {categoryLoading && <LoadingState label="正在加载分类库" />}
            {categories.map((category) => (
              <article className="category" key={category.id}>
                <h3>{category.name}</h3>
                <p>{category.description}</p>
                <div className="chips">{category.tags.split(',').map((tag) => <span key={tag}>{tag}</span>)}</div>
              </article>
            ))}
            {!categoryLoading && categories.length === 0 && <EmptyState title="没有匹配的分类" helper="换个关键词或清空筛选条件再试。" />}
          </div>
        </div>
        <div>
          <h3>宠物资料</h3>
          <div className="petGrid compactGrid">
            {petLoading && <LoadingState label="正在加载宠物资料" />}
            {pets.map((pet) => <PetCard key={pet.id} pet={pet} onOpen={onOpenPet} />)}
            {!petLoading && pets.length === 0 && <EmptyState title="没有匹配的宠物" helper="可以调整分类、城市、价格或关键词筛选。" />}
          </div>
        </div>
      </div>
    </section>
  );
}

function PetsPage({ loading, pets, onOpen }: { loading: boolean; pets: Pet[]; onOpen: (pet: Pet) => void }) {
  return (
    <section className="page section">
      <SectionTitle icon={<Store />} title="宠物展示与售卖" helper="查看宠物基础信息，后续可扩展订单与审核" />
      <div className="petGrid">
        {loading && <LoadingState label="正在加载宠物资料" />}
        {pets.map((pet) => <PetCard key={pet.id} pet={pet} onOpen={onOpen} />)}
        {!loading && pets.length === 0 && <EmptyState title="没有匹配的宠物" helper="可以调整分类、城市、价格或关键词筛选。" />}
      </div>
    </section>
  );
}

function MarketPage(props: {
  searchQuery: string;
  categoryFilter: string;
  cityFilter: string;
  typeFilter: string;
  minPrice: string;
  maxPrice: string;
  sortMode: 'latest' | 'oldest';
  loading: boolean;
  availableCategories: string[];
  availableCities: string[];
  postTypes: string[];
  posts: MarketPost[];
  categories: Category[];
  referenceData: ReferenceData;
  currentUser: UserProfile | null;
  onSearch: (value: string) => void;
  onCategory: (value: string) => void;
  onCity: (value: string) => void;
  onType: (value: string) => void;
  onMinPrice: (value: string) => void;
  onMaxPrice: (value: string) => void;
  onSort: (value: 'latest' | 'oldest') => void;
  onOpenPost: (post: MarketPost) => void;
  favoriteIds: Set<number>;
  onToggleFavorite: (post: MarketPost) => void;
  onPublished: () => void;
}) {
  return (
    <section className="page section split">
      <div>
        <SectionTitle icon={<Plus />} title="售卖 / 互换 / 领养帖子" helper="筛选交易内容，点击帖子查看详情并发起站内私信" />
        <FilterBar {...props} />
        {props.loading && <LoadingState label="正在加载交易帖子" />}
        <PostList posts={props.posts} currentUser={props.currentUser} favoriteIds={props.favoriteIds} onOpen={props.onOpenPost} onToggleFavorite={props.onToggleFavorite} />
      </div>
      <Composer categories={props.categories} referenceData={props.referenceData} currentUser={props.currentUser} onSuccess={props.onPublished} />
    </section>
  );
}

function MomentsPage({ loading, moments, onOpen }: { loading: boolean; moments: Moment[]; onOpen: (moment: Moment) => void }) {
  return (
    <section className="page section">
      <SectionTitle icon={<Camera />} title="用户日常分享" helper="记录宠物近况、养护经验和可爱的日常瞬间" />
      {loading && <LoadingState label="正在加载用户日常" />}
      {!loading && <MomentList moments={moments} onOpen={onOpen} />}
    </section>
  );
}

function MinePage(props: {
  currentUser: UserProfile | null;
  posts: MarketPost[];
  moments: Moment[];
  sentIntents: TradeIntent[];
  receivedIntents: TradeIntent[];
  onOpen: (detail: { type: 'post' | 'moment' | 'pet'; item: MarketPost | Moment | Pet }) => void;
  onEdit: (detail: { type: 'post' | 'moment'; item: MarketPost | Moment }) => void;
  onChanged: () => void;
  onIntentStatus: (intent: TradeIntent, status: string) => void;
}) {
  return (
    <section className="page section">
      <SectionTitle icon={<User />} title="我的发布" helper="登录后可集中查看并删除自己的交易帖和日常" />
      <MyPanel {...props} />
    </section>
  );
}

function FavoritesPage({ currentUser, posts, favoriteIds, onOpen, onToggleFavorite }: { currentUser: UserProfile | null; posts: MarketPost[]; favoriteIds: Set<number>; onOpen: (post: MarketPost) => void; onToggleFavorite: (post: MarketPost) => void }) {
  return (
    <section className="page section">
      <SectionTitle icon={<Heart />} title="我的收藏" helper="收藏会保存到数据库，方便后续继续查看和私信沟通" />
      {!currentUser ? <EmptyState title="登录后查看收藏" helper="收藏帖子需要登录，数据会保存在站内账号下。" /> : <PostList posts={posts} currentUser={currentUser} favoriteIds={favoriteIds} onOpen={onOpen} onToggleFavorite={onToggleFavorite} />}
    </section>
  );
}

function ProfilePage(props: { currentUser: UserProfile | null; referenceData: ReferenceData; posts: MarketPost[]; favoriteCount: number; onSaved: (user: UserProfile) => void; onFavorites: () => void; onMessages: () => void }) {
  return (
    <section className="page section">
      <SectionTitle icon={<User />} title="个人主页" helper="维护头像、简介、常驻城市，并集中进入收藏和私信" />
      <ProfilePanel {...props} />
    </section>
  );
}

function AdminPage({ currentUser, onOpen, onChanged }: { currentUser: UserProfile | null; onOpen: (detail: { type: 'post' | 'moment' | 'pet'; item: MarketPost | Moment | Pet }) => void; onChanged: () => void }) {
  const [tab, setTab] = React.useState<AdminTab>('reports');
  const [users, setUsers] = React.useState<UserProfile[]>([]);
  const [reports, setReports] = React.useState<ContentReport[]>([]);
  const [posts, setPosts] = React.useState<MarketPost[]>([]);
  const [moments, setMoments] = React.useState<Moment[]>([]);
  const [categories, setCategories] = React.useState<Category[]>([]);
  const [regionAreas, setRegionAreas] = React.useState<RegionArea[]>([]);
  const [loading, setLoading] = React.useState(false);
  const [notice, setNotice] = React.useState('');
  const adminName = currentUser?.nickname || '';
  const adminQuery = `admin=${encodeURIComponent(adminName)}`;

  const load = React.useCallback(() => {
    if (!isSuperAdmin(currentUser)) return;
    setLoading(true);
    Promise.all([
      fetch(`${API_BASE}/users?${adminQuery}`).then((res) => res.ok ? res.json() : []),
      fetch(`${API_BASE}/reports/admin?${adminQuery}`).then((res) => res.ok ? res.json() : []),
      fetch(`${API_BASE}/posts/admin?${adminQuery}`).then((res) => res.ok ? res.json() : []),
      fetch(`${API_BASE}/moments/admin?${adminQuery}`).then((res) => res.ok ? res.json() : []),
      fetch(`${API_BASE}/categories`).then((res) => res.ok ? res.json() : []),
      fetch(`${API_BASE}/reference-data/regions/admin?${adminQuery}`).then((res) => res.ok ? res.json() : [])
    ])
      .then(([nextUsers, nextReports, nextPosts, nextMoments, nextCategories, nextRegions]) => {
        setUsers(nextUsers);
        setReports(nextReports);
        setPosts(nextPosts);
        setMoments(nextMoments);
        setCategories(nextCategories);
        setRegionAreas(nextRegions);
      })
      .catch(() => setNotice('管理数据加载失败，请确认后端服务已启动。'))
      .finally(() => setLoading(false));
  }, [currentUser, adminQuery]);

  React.useEffect(load, [load]);

  if (!currentUser) {
    return <section className="page section"><EmptyState title="登录后进入管理后台" helper="请使用超级管理员账号和管理员口令登录。" /></section>;
  }
  if (!isSuperAdmin(currentUser)) {
    return <section className="page section"><EmptyState title="无权访问管理后台" helper="普通用户只能管理自己的发布、收藏和私信。" /></section>;
  }
  const operator = currentUser.nickname;

  async function updateUser(user: UserProfile, blocked: boolean) {
    const reason = encodeURIComponent(blocked ? '管理员处理违规行为' : '');
    const res = await fetch(`${API_BASE}/users/${user.id}/${blocked ? 'blacklist' : 'unblacklist'}?admin=${encodeURIComponent(operator)}${blocked ? `&reason=${reason}` : ''}`, { method: 'PUT' });
    setNotice(res.ok ? (blocked ? '用户已拉黑' : '用户已解除限制') : await readError(res));
    load();
  }

  async function audit(kind: 'posts' | 'moments', id: number, status: string) {
    const res = await fetch(`${API_BASE}/${kind}/${id}/audit?admin=${encodeURIComponent(operator)}&status=${encodeURIComponent(status)}`, { method: 'PUT' });
    setNotice(res.ok ? `内容已${status}` : await readError(res));
    load();
    onChanged();
  }

  async function handleReport(report: ContentReport, action: string) {
    const params = new URLSearchParams({
      operator,
      status: '已处理',
      action,
      note: action.includes('Block') ? '举报处理后限制作者' : '举报处理完成'
    });
    const res = await fetch(`${API_BASE}/reports/${report.id}/handle?${params.toString()}`, { method: 'PUT' });
    setNotice(res.ok ? '举报已处理' : await readError(res));
    load();
    onChanged();
  }

  async function saveCategory(category?: Category) {
    const name = window.prompt('分类名称', category?.name || '');
    if (!name?.trim()) return;
    const description = window.prompt('分类描述', category?.description || '') || '';
    const tags = window.prompt('标签，使用逗号分隔', category?.tags || '') || '';
    const res = await fetch(`${API_BASE}/categories${category ? `/${category.id}` : ''}?admin=${encodeURIComponent(operator)}`, {
      method: category ? 'PUT' : 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, description, tags, imageUrl: '' })
    });
    setNotice(res.ok ? '分类已保存' : await readError(res));
    load();
  }

  async function deleteCategory(category: Category) {
    if (!window.confirm(`确认删除分类「${category.name}」吗？`)) return;
    const res = await fetch(`${API_BASE}/categories/${category.id}?admin=${encodeURIComponent(operator)}`, { method: 'DELETE' });
    setNotice(res.ok ? '分类已删除' : await readError(res));
    load();
  }

  async function saveRegion(level: RegionArea['level'], parentId?: number, region?: RegionArea) {
    const name = window.prompt(region ? '修改地区名称' : '新增地区名称', region?.name || '');
    if (!name?.trim()) return;
    const res = await fetch(`${API_BASE}/reference-data/regions${region ? `/${region.id}` : ''}?admin=${encodeURIComponent(operator)}`, {
      method: region ? 'PUT' : 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, level, parentId, sortOrder: region?.sortOrder })
    });
    setNotice(res.ok ? '地区已保存' : await readError(res));
    load();
  }

  async function deleteRegion(region: RegionArea) {
    if (!window.confirm(`确认删除地区「${region.name}」及其下级地区吗？`)) return;
    const res = await fetch(`${API_BASE}/reference-data/regions/${region.id}?admin=${encodeURIComponent(operator)}`, { method: 'DELETE' });
    setNotice(res.ok ? '地区已删除' : await readError(res));
    load();
  }

  const provinces = regionAreas.filter((region) => region.level === 'province');

  return (
    <section className="page section">
      <SectionTitle icon={<ShieldCheck />} title="管理后台" helper="集中处理举报、审核内容和限制违规用户" />
      {loading && <LoadingState label="正在加载管理数据" />}
      {notice && <p className="formNote adminNotice">{notice}</p>}
      <div className="adminTabs">
        <button type="button" className={tab === 'reports' ? 'active' : ''} onClick={() => setTab('reports')}>举报处理 <span>{reports.length}</span></button>
        <button type="button" className={tab === 'users' ? 'active' : ''} onClick={() => setTab('users')}>用户管理 <span>{users.length}</span></button>
        <button type="button" className={tab === 'posts' ? 'active' : ''} onClick={() => setTab('posts')}>帖子审核 <span>{posts.length}</span></button>
        <button type="button" className={tab === 'moments' ? 'active' : ''} onClick={() => setTab('moments')}>日常审核 <span>{moments.length}</span></button>
        <button type="button" className={tab === 'categories' ? 'active' : ''} onClick={() => setTab('categories')}>分类管理 <span>{categories.length}</span></button>
        <button type="button" className={tab === 'regions' ? 'active' : ''} onClick={() => setTab('regions')}>地区库 <span>{provinces.length}</span></button>
      </div>
      <div className="adminGrid">
        {tab === 'reports' && <AdminPanel title="举报处理">
          {reports.length ? reports.map((report) => (
            <article className="adminItem" key={report.id}>
              <div className="between"><strong>{report.targetType === 'post' ? '交易帖举报' : '日常举报'} #{report.targetId}</strong><span className={`intentStatus ${report.status === '待处理' ? 'pending' : 'accepted'}`}>{report.status}</span></div>
              <p>{report.reason}</p>
              <div className="intentMeta"><span>举报人 {report.reporter}</span><span>{formatTime(report.createdAt)}</span></div>
              <div className="intentActions">
                <button type="button" onClick={() => handleReport(report, 'removeTarget')}>下架内容</button>
                <button type="button" onClick={() => handleReport(report, 'removeAndBlockAuthor')}>下架并拉黑作者</button>
                <button type="button" onClick={() => handleReport(report, 'none')}>仅标记处理</button>
              </div>
            </article>
          )) : <p className="emptyState">暂无举报记录。</p>}
        </AdminPanel>}
        {tab === 'users' && <AdminPanel title="用户管理">
          {users.map((user) => (
            <article className="adminItem compact" key={user.id}>
              <div><strong>{user.nickname}</strong><p>{user.city || '未设置城市'} · {user.bio || '暂无简介'}</p></div>
              <span className={user.blacklisted ? 'auditBadge removed' : 'auditBadge'}>{user.blacklisted ? '已限制' : '正常'}</span>
              <button type="button" onClick={() => updateUser(user, !user.blacklisted)}>{user.blacklisted ? '解除限制' : '拉黑'}</button>
            </article>
          ))}
        </AdminPanel>}
        {tab === 'posts' && <AdminPanel title="帖子审核">
          {posts.map((post) => (
            <article className="adminItem" key={post.id}>
              <div className="between"><strong>{post.title}</strong><AuditBadge value={post.auditStatus} /></div>
              <p>{post.description}</p>
              <div className="intentMeta"><span>{post.author}</span><span>{post.category}</span><span>{formatTime(post.createdAt || '')}</span></div>
              <div className="intentActions">
                <button type="button" onClick={() => onOpen({ type: 'post', item: post })}>查看</button>
                <button type="button" onClick={() => audit('posts', post.id, '审核通过')}>恢复展示</button>
                <button type="button" onClick={() => audit('posts', post.id, '已下架')}>下架</button>
              </div>
            </article>
          ))}
        </AdminPanel>}
        {tab === 'moments' && <AdminPanel title="日常审核">
          {moments.map((moment) => (
            <article className="adminItem" key={moment.id}>
              <div className="between"><strong>{moment.petName} 的日常</strong><AuditBadge value={moment.auditStatus} /></div>
              <p>{moment.content}</p>
              <div className="intentMeta"><span>{moment.author}</span><span>{moment.category || '日常'}</span><span>{formatTime(moment.createdAt || '')}</span></div>
              <div className="intentActions">
                <button type="button" onClick={() => onOpen({ type: 'moment', item: moment })}>查看</button>
                <button type="button" onClick={() => audit('moments', moment.id, '审核通过')}>恢复展示</button>
                <button type="button" onClick={() => audit('moments', moment.id, '已下架')}>下架</button>
              </div>
            </article>
          ))}
        </AdminPanel>}
        {tab === 'categories' && <AdminPanel title="分类管理">
          <div className="intentActions"><button type="button" onClick={() => saveCategory()}>新增分类</button></div>
          {categories.map((category) => (
            <article className="adminItem" key={category.id}>
              <div className="between"><strong>{category.name}</strong><span className="auditBadge">{category.tags || '未设置标签'}</span></div>
              <p>{category.description}</p>
              <div className="intentActions">
                <button type="button" onClick={() => saveCategory(category)}>编辑</button>
                <button type="button" onClick={() => deleteCategory(category)}>删除</button>
              </div>
            </article>
          ))}
        </AdminPanel>}
        {tab === 'regions' && <AdminPanel title="地区库管理">
          <div className="intentActions"><button type="button" onClick={() => saveRegion('province')}>新增省份</button></div>
          {provinces.map((province) => (
            <article className="adminItem" key={province.id}>
              <div className="between"><strong>{province.name}</strong><span className="auditBadge">省份</span></div>
              <RegionChildren regions={regionAreas} parent={province} onSave={saveRegion} onDelete={deleteRegion} />
              <div className="intentActions">
                <button type="button" onClick={() => saveRegion('city', province.id)}>新增城市</button>
                <button type="button" onClick={() => saveRegion('province', undefined, province)}>编辑省份</button>
                <button type="button" onClick={() => deleteRegion(province)}>删除省份</button>
              </div>
            </article>
          ))}
        </AdminPanel>}
      </div>
    </section>
  );
}

function AdminPanel({ title, children }: { title: string; children: React.ReactNode }) {
  return <div className="adminPanel"><h3>{title}</h3>{children}</div>;
}

function RegionChildren({ regions, parent, onSave, onDelete }: { regions: RegionArea[]; parent: RegionArea; onSave: (level: RegionArea['level'], parentId?: number, region?: RegionArea) => void; onDelete: (region: RegionArea) => void }) {
  const children = regions.filter((region) => region.parentId === parent.id);
  if (!children.length) return <p className="emptyState">暂无下级地区。</p>;
  return (
    <div className="regionAdminList">
      {children.map((region) => (
        <div className="regionAdminItem" key={region.id}>
          <strong>{region.name}</strong>
          <span>{region.level === 'city' ? '城市' : '区县'}</span>
          {region.level === 'city' && <button type="button" onClick={() => onSave('district', region.id)}>新增区县</button>}
          <button type="button" onClick={() => onSave(region.level, region.parentId, region)}>编辑</button>
          <button type="button" onClick={() => onDelete(region)}>删除</button>
          {region.level === 'city' && <div className="regionDistricts">
            {regions.filter((item) => item.parentId === region.id).map((district) => <button type="button" key={district.id} onClick={() => onSave('district', region.id, district)}>{district.name}</button>)}
          </div>}
        </div>
      ))}
    </div>
  );
}

function FilterBar(props: {
  searchQuery: string;
  categoryFilter: string;
  cityFilter: string;
  typeFilter: string;
  minPrice: string;
  maxPrice: string;
  sortMode: 'latest' | 'oldest';
  availableCategories: string[];
  availableCities: string[];
  postTypes: string[];
  onSearch: (value: string) => void;
  onCategory: (value: string) => void;
  onCity: (value: string) => void;
  onType: (value: string) => void;
  onMinPrice: (value: string) => void;
  onMaxPrice: (value: string) => void;
  onSort: (value: 'latest' | 'oldest') => void;
}) {
  return (
    <div className="filterPanel">
      <div className="search"><Search size={20} /><input value={props.searchQuery} onChange={(event) => props.onSearch(event.target.value)} placeholder="搜索猫咪、柯基、互换、领养、闲置用品" /></div>
      <div className="quickFilters">
        <select value={props.categoryFilter} onChange={(event) => props.onCategory(event.target.value)}>
          <option value="全部">全部分类</option>
          {props.availableCategories.map((category) => <option key={category}>{category}</option>)}
        </select>
        <select value={props.cityFilter} onChange={(event) => props.onCity(event.target.value)}>
          <option value="全部">全部城市</option>
          {props.availableCities.map((city) => <option key={city}>{city}</option>)}
        </select>
        <select value={props.typeFilter} onChange={(event) => props.onType(event.target.value)}>
          <option value="全部">全部类型</option>
          {props.postTypes.map((type) => <option key={type}>{type}</option>)}
        </select>
        <input type="number" min="0" value={props.minPrice} onChange={(event) => props.onMinPrice(event.target.value)} placeholder="最低价" />
        <input type="number" min="0" value={props.maxPrice} onChange={(event) => props.onMaxPrice(event.target.value)} placeholder="最高价" />
        <select value={props.sortMode} onChange={(event) => props.onSort(event.target.value as 'latest' | 'oldest')}>
          <option value="latest">最新发布</option>
          <option value="oldest">最早发布</option>
        </select>
      </div>
    </div>
  );
}

function PetCard({ pet, onOpen }: { pet: Pet; onOpen: (pet: Pet) => void }) {
  const healthTags = petHealthTags(pet).slice(0, 3);
  return (
    <article className="petCard clickable" onClick={() => onOpen(pet)}>
      {imageBox(primaryImage(pet), pet.name)}
      <div className="petInfo">
        <div className="between"><h3>{pet.name}</h3><span className="status">{pet.status}</span></div>
        <p className="petBreed">{pet.breed} · {pet.gender || '未知'} · {pet.ageRange || pet.age}</p>
        <p className="sub"><MapPin size={15} />{pet.city}</p>
        <p>{pet.personality}</p>
        {healthTags.length > 0 && <div className="miniChips">{healthTags.map((tag) => <span key={tag}>{tag}</span>)}</div>}
        <div className="between cardFooter">
          <span className="price">{formatPrice(pet.price)}</span>
          <span className="health"><ShieldCheck size={15} />{pet.ownerName || pet.healthInfo}</span>
        </div>
      </div>
    </article>
  );
}

function PostList({ posts, currentUser, favoriteIds, onOpen, onToggleFavorite }: { posts: MarketPost[]; currentUser: UserProfile | null; favoriteIds: Set<number>; onOpen: (post: MarketPost) => void; onToggleFavorite: (post: MarketPost) => void }) {
  if (posts.length === 0) {
    return <EmptyState title="没有匹配的交易帖" helper="可以调整交易类型、城市、分类或关键词。" />;
  }

  return (
    <div className="postList">
      {posts.map((post) => (
        <article className="post clickable" key={post.id} onClick={() => onOpen(post)}>
          <div className="between">
            <div className="inlineBadges"><span className="type">{post.type}</span><span className="status">{post.status || '在售'}</span><AuditBadge value={post.auditStatus} /></div>
            <button type="button" className={favoriteIds.has(post.id) ? 'favoriteButton active' : 'favoriteButton'} title={currentUser ? '收藏帖子' : '登录后收藏'} onClick={(event) => { event.stopPropagation(); onToggleFavorite(post); }}><Heart size={15} />{favoriteIds.has(post.id) ? '已收藏' : '收藏'}</button>
          </div>
          <h3>{post.title}</h3>
          <p>{post.description}</p>
          <div className="between cardFooter">
            <div className="postMeta"><span>{post.category}</span><span>{post.city}</span><span>{post.author}</span></div>
            <div className="postSideMeta"><span className="price">{formatPrice(post.price)}</span><span className="postContact"><MessageCircle size={15} />{post.contact || CONTACT_VALUE}</span></div>
          </div>
        </article>
      ))}
    </div>
  );
}

function MomentList({ moments, onOpen }: { moments: Moment[]; onOpen: (moment: Moment) => void }) {
  if (moments.length === 0) {
    return <EmptyState title="没有匹配的日常" helper="可以调整城市、分类或关键词筛选。" />;
  }

  return (
    <div className="momentGrid">
      {moments.map((moment) => (
        <article className="moment clickable" key={moment.id} onClick={() => onOpen(moment)}>
          {imageBox(primaryImage(moment), moment.petName || moment.author)}
          <div><div className="inlineBadges"><span className="type">{moment.category || '日常'}</span><AuditBadge value={moment.auditStatus} /></div><h3>{moment.author} 和 {moment.petName}</h3><p>{moment.content}</p><div className="momentMeta"><span>{moment.city || '未选择地区'}</span><span className="likes"><Heart size={15} />{moment.likes}</span></div></div>
        </article>
      ))}
    </div>
  );
}

function MyPanel(props: {
  currentUser: UserProfile | null;
  posts: MarketPost[];
  moments: Moment[];
  sentIntents: TradeIntent[];
  receivedIntents: TradeIntent[];
  onOpen: (detail: { type: 'post' | 'moment' | 'pet'; item: MarketPost | Moment | Pet }) => void;
  onEdit: (detail: { type: 'post' | 'moment'; item: MarketPost | Moment }) => void;
  onChanged: () => void;
  onIntentStatus: (intent: TradeIntent, status: string) => void;
}) {
  if (!props.currentUser) return <div className="myPanel emptyState">登录后，这里会显示你发布的交易帖和日常。</div>;
  if (props.currentUser.blacklisted) return <div className="myPanel emptyState">{props.currentUser.blacklistReason || '账号已被限制，暂不能发布或互动。'}</div>;
  const myPosts = props.posts.filter((post) => post.author === props.currentUser!.nickname);
  const myMoments = props.moments.filter((moment) => moment.author === props.currentUser!.nickname);

  async function remove(kind: 'posts' | 'moments', id: number) {
    if (!window.confirm('确认删除这条内容吗？删除后不可恢复。')) return;
    const author = encodeURIComponent(props.currentUser!.nickname);
    const res = await fetch(`${API_BASE}/${kind}/${id}?author=${author}`, { method: 'DELETE' });
    if (!res.ok) alert(await readError(res));
    props.onChanged();
  }

  async function closePost(post: MarketPost) {
    if (!window.confirm('确认关闭这条交易帖吗？关闭后仍可在我的发布中查看。')) return;
    const author = encodeURIComponent(props.currentUser!.nickname);
    const res = await fetch(`${API_BASE}/posts/${post.id}?author=${author}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ ...post, status: '已关闭' })
    });
    if (!res.ok) alert(await readError(res));
    props.onChanged();
  }

  return (
    <div className="myPanel">
      <div className="mySummary">
        <div><strong>{props.receivedIntents.length}</strong><span>收到的意向</span></div>
        <div><strong>{props.sentIntents.length}</strong><span>我的意向</span></div>
        <div><strong>{myPosts.length}</strong><span>我的帖子</span></div>
        <div><strong>{myMoments.length}</strong><span>我的日常</span></div>
      </div>
      <div className="myColumns">
        <div>
          <h3>交易帖</h3>
          {myPosts.length ? myPosts.map((post) => (
            <div className="mineRow" key={post.id}>
              <button type="button" onClick={() => props.onOpen({ type: 'post', item: post })}>{post.title}<AuditBadge value={post.auditStatus} /></button>
              <button type="button" className="editIcon" title="编辑" onClick={() => props.onEdit({ type: 'post', item: post })}><Pencil size={16} /></button>
              <button type="button" className="closeTradeIcon" title="关闭交易" disabled={(post.status || '在售') === '已关闭'} onClick={() => closePost(post)}>关</button>
              <button type="button" className="dangerIcon" onClick={() => remove('posts', post.id)}><Trash2 size={16} /></button>
            </div>
          )) : <p className="emptyState">还没有发布交易帖。</p>}
        </div>
        <div>
          <h3>日常</h3>
          {myMoments.length ? myMoments.map((moment) => (
            <div className="mineRow" key={moment.id}>
              <button type="button" onClick={() => props.onOpen({ type: 'moment', item: moment })}>{moment.petName} 的日常<AuditBadge value={moment.auditStatus} /></button>
              <button type="button" className="editIcon" title="编辑" onClick={() => props.onEdit({ type: 'moment', item: moment })}><Pencil size={16} /></button>
              <button type="button" className="dangerIcon" onClick={() => remove('moments', moment.id)}><Trash2 size={16} /></button>
            </div>
          )) : <p className="emptyState">还没有发布日常。</p>}
        </div>
      </div>
      <TradeIntentPanel
        sentIntents={props.sentIntents}
        receivedIntents={props.receivedIntents}
        onOpenPost={(post) => props.onOpen({ type: 'post', item: post })}
        onIntentStatus={props.onIntentStatus}
      />
    </div>
  );
}

function TradeIntentPanel({ sentIntents, receivedIntents, onOpenPost, onIntentStatus }: { sentIntents: TradeIntent[]; receivedIntents: TradeIntent[]; onOpenPost: (post: MarketPost) => void; onIntentStatus: (intent: TradeIntent, status: string) => void }) {
  return (
    <div className="intentBoard">
      <div>
        <h3>收到的交易意向</h3>
        {receivedIntents.length ? receivedIntents.map((intent) => (
          <IntentCard key={intent.id} intent={intent} mode="received" onOpenPost={onOpenPost} onIntentStatus={onIntentStatus} />
        )) : <p className="emptyState">还没有买家提交意向。</p>}
      </div>
      <div>
        <h3>我提交的意向</h3>
        {sentIntents.length ? sentIntents.map((intent) => (
          <IntentCard key={intent.id} intent={intent} mode="sent" onOpenPost={onOpenPost} onIntentStatus={onIntentStatus} />
        )) : <p className="emptyState">还没有提交过交易意向。</p>}
      </div>
    </div>
  );
}

function IntentCard({ intent, mode, onOpenPost, onIntentStatus }: { intent: TradeIntent; mode: 'sent' | 'received'; onOpenPost: (post: MarketPost) => void; onIntentStatus: (intent: TradeIntent, status: string) => void }) {
  const isPending = intent.status === '待处理';
  return (
    <article className="intentCard">
      <div className="between">
        <strong>{intent.postTitle}</strong>
        <span className={`intentStatus ${statusClass(intent.status)}`}>{intent.status}</span>
      </div>
      <p>{intent.message}</p>
      <div className="intentMeta">
        <span>{mode === 'received' ? `来自 ${intent.requester}` : `发布者 ${intent.owner}`}</span>
        <span>{formatTime(intent.updatedAt || intent.createdAt)}</span>
      </div>
      <div className="intentActions">
        {intent.post && <button type="button" onClick={() => onOpenPost(intent.post!)}>查看帖子</button>}
        {mode === 'received' && isPending && <>
          <button type="button" onClick={() => onIntentStatus(intent, '已同意')}><CheckCircle2 size={15} />同意</button>
          <button type="button" onClick={() => onIntentStatus(intent, '已拒绝')}>拒绝</button>
        </>}
        {mode === 'sent' && isPending && <button type="button" onClick={() => onIntentStatus(intent, '已取消')}>取消意向</button>}
      </div>
    </article>
  );
}

function Composer({ categories, referenceData, currentUser, onSuccess }: { categories: Category[]; referenceData: ReferenceData; currentUser: UserProfile | null; onSuccess: () => void }) {
  const [mode, setMode] = React.useState<'post' | 'moment'>('post');
  const [postType, setPostType] = React.useState(referenceData.postTypes[0] || '互换');
  const [imageUrls, setImageUrls] = React.useState<string[]>([]);
  const [imagePreviews, setImagePreviews] = React.useState<string[]>([]);
  const imagePreviewRef = React.useRef<string[]>([]);
  const [busy, setBusy] = React.useState(false);
  const [error, setError] = React.useState('');
  const regions = referenceData.regions.length ? referenceData.regions : fallbackRegions;
  const [province, setProvince] = React.useState(regions[0].name);
  const selectedProvince = regions.find((item) => item.name === province) || regions[0];
  const [city, setCity] = React.useState(selectedProvince.cities[0].name);
  const selectedCity = selectedProvince.cities.find((item) => item.name === city) || selectedProvince.cities[0];
  const [district, setDistrict] = React.useState(selectedCity.districts[0]);
  const canPublish = Boolean(currentUser && !currentUser.blacklisted);
  const typeConfig = tradeTypeConfigs[postType] || {
    title: '发布交易帖',
    priceLabel: '价格',
    description: '描述需求、宠物状态或交易条件。',
    fields: [{ name: 'preferredTime', label: '期望时间', placeholder: '例如本周末可看宠或自提' }]
  };

  React.useEffect(() => {
    const nextCity = selectedProvince.cities[0].name;
    setCity(nextCity);
    setDistrict(selectedProvince.cities[0].districts[0]);
  }, [province]);

  React.useEffect(() => setDistrict(selectedCity.districts[0]), [city]);

  React.useEffect(() => {
    if (!regions.some((item) => item.name === province)) {
      setProvince(regions[0].name);
      setCity(regions[0].cities[0].name);
      setDistrict(regions[0].cities[0].districts[0]);
    }
  }, [regions, province]);

  React.useEffect(() => {
    imagePreviewRef.current = imagePreviews;
  }, [imagePreviews]);

  React.useEffect(() => () => {
    imagePreviewRef.current.forEach((preview) => URL.revokeObjectURL(preview));
  }, []);

  React.useEffect(() => {
    if (mode === 'post' && referenceData.postTypes.length && !referenceData.postTypes.includes(postType)) {
      setPostType(referenceData.postTypes[0]);
    }
  }, [mode, referenceData.postTypes, postType]);

  async function upload(files: FileList) {
    setError('');
    const nextFiles = Array.from(files).slice(0, Math.max(0, 6 - imageUrls.length));
    if (nextFiles.length === 0) return setError('最多上传 6 张图片。');
    for (const file of nextFiles) {
      const validationError = validateImage(file);
      if (validationError) return setError(validationError);
    }
    try {
      for (const file of nextFiles) {
        const previewUrl = URL.createObjectURL(file);
        setImagePreviews((items) => [...items, previewUrl]);
        const form = new FormData();
        form.append('file', file);
        const res = await fetch(`${API_BASE}/upload`, { method: 'POST', body: form });
        if (!res.ok) throw new Error(await readError(res));
        const data = await res.json();
        setImageUrls((items) => [...items, data.url]);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : '图片上传失败，请稍后重试。');
    }
  }

  function removeImage(index: number) {
    setImagePreviews((items) => {
      const next = [...items];
      const [removed] = next.splice(index, 1);
      if (removed) URL.revokeObjectURL(removed);
      return next;
    });
    setImageUrls((items) => items.filter((_, itemIndex) => itemIndex !== index));
  }

  async function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError('');
    const formElement = event.currentTarget;
    if (!currentUser) return setError('请先登录后再发布内容。');
    if (currentUser.blacklisted) return setError(currentUser.blacklistReason || '账号已被限制，暂不能发布内容。');
    const form = new FormData(formElement);
    const body = Object.fromEntries(form.entries());
    if (hasUnsafeContent(Object.values(body).join(' '))) return setError('请不要填写手机号、微信号、QQ 号或敏感词，平台只允许站内沟通。');
    setBusy(true);
    body.author = currentUser.nickname;
    body.city = `${province} ${city} ${district}`;
    body.contact = CONTACT_VALUE;
    if (mode === 'post') {
      body.type = postType;
      body.price = body.price || '0';
      body.description = withTradeDetails(String(body.description || ''), typeConfig.fields, body);
      typeConfig.fields.forEach((field) => delete body[field.name]);
    }
    if (imageUrls.length) {
      body.imageUrl = imageUrls[0];
      body.imageUrls = imageUrls.join(',');
    }
    try {
      const res = await fetch(`${API_BASE}/${mode === 'post' ? 'posts' : 'moments'}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
      });
      if (!res.ok) throw new Error(await readError(res));
      formElement.reset();
      imagePreviews.forEach((preview) => URL.revokeObjectURL(preview));
      setImageUrls([]);
      setImagePreviews([]);
      onSuccess();
    } catch (err) {
      setError(err instanceof Error ? err.message : '发布失败，请检查内容是否符合规则。');
    } finally {
      setBusy(false);
    }
  }

  return (
    <aside className="composer">
      <div className="composerHeader"><h3>发布中心</h3><span>{currentUser ? `当前用户：${currentUser.nickname}` : '登录后可发布'}</span></div>
      <div className="tabs"><button type="button" className={mode === 'post' ? 'active' : ''} onClick={() => setMode('post')}>交易帖</button><button type="button" className={mode === 'moment' ? 'active' : ''} onClick={() => setMode('moment')}>日常</button></div>
      {!currentUser && <div className="locked"><Lock size={18} /><span>请先在右上角登录，之后才能发布帖子或日常。</span></div>}
      {currentUser?.blacklisted && <div className="locked"><Lock size={18} /><span>{currentUser.blacklistReason || '账号已被限制，暂不能发布帖子或日常。'}</span></div>}
      <form onSubmit={submit}>
        {mode === 'post' ? (
          <>
            <input name="title" placeholder="标题" required disabled={!canPublish} />
            <select name="type" value={postType} disabled={!canPublish} onChange={(event) => setPostType(event.target.value)}>{referenceData.postTypes.map((type) => <option key={type}>{type}</option>)}</select>
            <div className="typeHint"><strong>{typeConfig.title}</strong><span>{typeConfig.description}</span></div>
            <select name="status" defaultValue="在售" disabled={!canPublish}>{tradeStatuses.map((status) => <option key={status}>{status}</option>)}</select>
            <input name="price" type="number" min="0" step="1" placeholder={`${typeConfig.priceLabel}，面议可填 0`} disabled={!canPublish} />
            {typeConfig.fields.map((field) => <input key={field.name} name={field.name} placeholder={`${field.label}：${field.placeholder}`} required={field.required} disabled={!canPublish} />)}
          </>
        ) : <input name="petName" placeholder="宠物名字" required disabled={!canPublish} />}
        <select name="category" required disabled={!canPublish}>{categories.map((category) => <option key={category.id} value={category.name}>{category.name}</option>)}</select>
        <RegionPicker province={province} city={city} district={district} selectedProvince={selectedProvince} selectedCity={selectedCity} regions={regions} disabled={!canPublish} onProvince={setProvince} onCity={setCity} onDistrict={setDistrict} />
        {mode === 'post' ? <textarea name="description" placeholder={`${typeConfig.description} 请勿填写手机号。`} required disabled={!canPublish} /> : <textarea name="content" placeholder="分享今天的宠物日常。请勿填写手机号。" required disabled={!canPublish} />}
        <div className="contactOnly"><MessageCircle size={16} /><span>联系方式固定为站内私信</span></div>
        <label className="fileInput"><Camera size={18} /><span>{imageUrls.length ? `已上传 ${imageUrls.length} 张` : '上传图片'}</span><input type="file" accept="image/*" multiple disabled={!canPublish} onChange={(e) => e.target.files && upload(e.target.files)} /></label>
        <p className="fileHint">支持 JPG、PNG、WebP、GIF，最多 6 张，单张不超过 5MB。</p>
        {imagePreviews.length > 0 && <div className="imagePreviewGrid">{imagePreviews.map((preview, index) => <div className="imagePreview" key={preview}><img src={preview} alt={`上传预览 ${index + 1}`} /><button type="button" onClick={() => removeImage(index)}>移除</button></div>)}</div>}
        {error && <InlineError message={error} />}
        <button className="submit" disabled={busy || !canPublish}>{busy ? '发布中...' : '发布'}</button>
      </form>
    </aside>
  );
}

function EditModal(props: {
  detail: { type: 'post' | 'moment'; item: MarketPost | Moment };
  categories: Category[];
  referenceData: ReferenceData;
  currentUser: UserProfile | null;
  onClose: () => void;
  onSaved: () => void;
}) {
  const item = props.detail.item;
  const regions = props.referenceData.regions.length ? props.referenceData.regions : fallbackRegions;
  const initialRegion = parseRegion(props.detail.type === 'post' ? (item as MarketPost).city : (item as Moment).city, regions);
  const [province, setProvince] = React.useState(initialRegion.province);
  const selectedProvince = regions.find((region) => region.name === province) || regions[0];
  const [city, setCity] = React.useState(initialRegion.city);
  const selectedCity = selectedProvince.cities.find((regionCity) => regionCity.name === city) || selectedProvince.cities[0];
  const [district, setDistrict] = React.useState(initialRegion.district);
  const [error, setError] = React.useState('');
  const [busy, setBusy] = React.useState(false);

  React.useEffect(() => {
    const nextCity = selectedProvince.cities[0].name;
    setCity(nextCity);
    setDistrict(selectedProvince.cities[0].districts[0]);
  }, [province]);

  React.useEffect(() => setDistrict(selectedCity.districts[0]), [city]);

  async function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!props.currentUser) return setError('请先登录后再编辑内容。');
    const form = new FormData(event.currentTarget);
    const body = Object.fromEntries(form.entries());
    if (hasUnsafeContent(Object.values(body).join(' '))) return setError('请不要填写手机号、微信号、QQ 号或敏感词，平台只允许站内沟通。');
    setBusy(true);
    setError('');
    body.author = props.currentUser.nickname;
    body.city = `${province} ${city} ${district}`;
    if (props.detail.type === 'post') {
      body.contact = CONTACT_VALUE;
      body.price = body.price || '0';
      body.imageUrl = (item as MarketPost).imageUrl || '';
      body.imageUrls = (item as MarketPost).imageUrls || (item as MarketPost).imageUrl || '';
    } else {
      body.imageUrl = (item as Moment).imageUrl || '';
      body.imageUrls = (item as Moment).imageUrls || (item as Moment).imageUrl || '';
    }
    try {
      const kind = props.detail.type === 'post' ? 'posts' : 'moments';
      const author = encodeURIComponent(props.currentUser.nickname);
      const res = await fetch(`${API_BASE}/${kind}/${item.id}?author=${author}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
      });
      if (!res.ok) throw new Error(await readError(res));
      props.onSaved();
    } catch (err) {
      setError(err instanceof Error ? err.message : '保存失败，请检查内容后再试。');
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="modalBackdrop" onClick={props.onClose}>
      <article className="detailModal editModal" onClick={(event) => event.stopPropagation()}>
        <button type="button" className="closeButton" onClick={props.onClose}><X size={18} /></button>
        <h2>{props.detail.type === 'post' ? '编辑交易帖' : '编辑日常'}</h2>
        <form onSubmit={submit}>
          {props.detail.type === 'post' ? (
            <>
              <input name="title" defaultValue={(item as MarketPost).title} placeholder="标题" required />
              <select name="type" defaultValue={(item as MarketPost).type}>{props.referenceData.postTypes.map((type) => <option key={type}>{type}</option>)}</select>
              <select name="status" defaultValue={(item as MarketPost).status || '在售'}>{tradeStatuses.map((status) => <option key={status}>{status}</option>)}</select>
              <input name="price" type="number" min="0" step="1" defaultValue={(item as MarketPost).price || 0} placeholder="价格，互换或面议可填 0" />
            </>
          ) : <input name="petName" defaultValue={(item as Moment).petName} placeholder="宠物名字" required />}
          <select name="category" defaultValue={props.detail.type === 'post' ? (item as MarketPost).category : (item as Moment).category} required>
            {props.categories.map((category) => <option key={category.id} value={category.name}>{category.name}</option>)}
          </select>
          <RegionPicker province={province} city={city} district={district} selectedProvince={selectedProvince} selectedCity={selectedCity} regions={regions} disabled={false} onProvince={setProvince} onCity={setCity} onDistrict={setDistrict} />
          {props.detail.type === 'post'
            ? <textarea name="description" defaultValue={(item as MarketPost).description} placeholder="描述需求、宠物状态或交易条件。请勿填写手机号。" required />
            : <textarea name="content" defaultValue={(item as Moment).content} placeholder="分享今天的宠物日常。请勿填写手机号。" required />}
          <div className="contactOnly"><MessageCircle size={16} /><span>联系方式固定为站内私信</span></div>
          {error && <p className="formError">{error}</p>}
          <button className="submit" disabled={busy}>{busy ? '保存中...' : '保存修改'}</button>
        </form>
      </article>
    </div>
  );
}

function ProfilePanel(props: {
  currentUser: UserProfile | null;
  referenceData: ReferenceData;
  posts: MarketPost[];
  favoriteCount: number;
  onSaved: (user: UserProfile) => void;
  onFavorites: () => void;
  onMessages: () => void;
}) {
  const [avatarUrl, setAvatarUrl] = React.useState(props.currentUser?.avatarUrl || '');
  const [bio, setBio] = React.useState(props.currentUser?.bio || '');
  const [city, setCity] = React.useState(props.currentUser?.city || '');
  const [status, setStatus] = React.useState('');
  const regions = props.referenceData.regions.length ? props.referenceData.regions : fallbackRegions;
  const cities = cityOptions(regions);
  const myPosts = props.currentUser ? props.posts.filter((post) => post.author === props.currentUser!.nickname) : [];

  React.useEffect(() => {
    setAvatarUrl(props.currentUser?.avatarUrl || '');
    setBio(props.currentUser?.bio || '');
    setCity(props.currentUser?.city || '');
  }, [props.currentUser]);

  if (!props.currentUser) {
    return <EmptyState title="登录后查看个人主页" helper="个人资料、收藏入口和私信入口会集中显示在这里。" />;
  }
  const accountLocked = Boolean(props.currentUser.blacklisted);

  async function saveProfile(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setStatus('');
    if (hasUnsafeContent(`${avatarUrl} ${bio} ${city}`)) {
      return setStatus('个人资料不能填写手机号、微信号、QQ 号或敏感词，请使用站内沟通。');
    }
    try {
      const res = await fetch(`${API_BASE}/users/${props.currentUser!.id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ avatarUrl, bio, city })
      });
      if (!res.ok) throw new Error(await readError(res));
      props.onSaved(await res.json());
      setStatus('资料已保存');
    } catch (err) {
      setStatus(err instanceof Error ? err.message : '保存失败，请稍后重试。');
    }
  }

  return (
    <div className="profilePanel">
      <div className="profileCard">
        <div className="avatarBox">{avatarUrl ? <img src={avatarUrl} alt={props.currentUser.nickname} /> : <span>{props.currentUser.nickname.slice(0, 1)}</span>}</div>
        <div>
          <h3>{props.currentUser.nickname}</h3>
          <p>{bio || '还没有填写个人简介。'}</p>
          <p className="sub"><MapPin size={15} />{city || '未设置常驻城市'}</p>
        </div>
      </div>
      <form className="profileForm" onSubmit={saveProfile}>
        {accountLocked && <p className="formError">{props.currentUser.blacklistReason || '账号已被限制，暂不能修改个人资料。'}</p>}
        <input value={avatarUrl} onChange={(event) => setAvatarUrl(event.target.value)} placeholder="头像图片地址" disabled={accountLocked} />
        <select value={city} onChange={(event) => setCity(event.target.value)} disabled={accountLocked}>
          <option value="">选择常驻城市</option>
          {cities.map((item) => <option key={item}>{item}</option>)}
        </select>
        <textarea value={bio} onChange={(event) => setBio(event.target.value)} placeholder="简介，例如养宠经验、偏好的宠物类型或交易习惯" disabled={accountLocked} />
        {status && <p className="formNote">{status}</p>}
        <button className="submit" type="submit" disabled={accountLocked}>保存个人资料</button>
      </form>
      <div className="profileActions">
        <button type="button" onClick={props.onFavorites}><Heart size={18} /><strong>我的收藏</strong><span>{props.favoriteCount} 条收藏</span></button>
        <button type="button" onClick={props.onMessages}><MessageCircle size={18} /><strong>我的私信</strong><span>从帖子详情联系发布者</span></button>
        <a href="#mine"><Store size={18} /><strong>我的交易</strong><span>{myPosts.length} 条发布</span></a>
      </div>
    </div>
  );
}

function MessagesPage({ currentUser, threads, onThreadsChange, onReload }: { currentUser: UserProfile | null; threads: MessageThread[]; onThreadsChange: (threads: MessageThread[]) => void; onReload: () => void }) {
  const [activeId, setActiveId] = React.useState(threads[0]?.id || '');
  const [draft, setDraft] = React.useState('');
  const [error, setError] = React.useState('');
  const activeThread = threads.find((thread) => thread.id === activeId) || threads[0];

  React.useEffect(() => {
    if (!threads.length) {
      setActiveId('');
      return;
    }
    if (!activeThread) {
      setActiveId(threads[0].id);
    }
  }, [threads, activeThread]);

  React.useEffect(() => {
    if (!activeThread || !currentUser) return;
    fetch(`${API_BASE}/messages/${activeThread.id}/read?user=${encodeURIComponent(currentUser.nickname)}`, { method: 'PUT' })
      .then((res) => res.ok ? res.json() : Promise.reject())
      .then((updated) => {
        onThreadsChange(threads.map((thread) => thread.id === updated.id ? updated : thread));
      })
      .catch(() => undefined);
  }, [activeId, currentUser]);

  if (!currentUser) {
    return <section className="page section"><EmptyState title="登录后查看站内私信" helper="私信只在站内沟通，不展示手机号等线下联系方式。" /></section>;
  }

  async function sendMessage(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const content = draft.trim();
    setError('');
    if (!currentUser) return;
    if (!activeThread || !content) return;
    if (hasUnsafeContent(content)) {
      setError('私信内容不能填写手机号、微信号、QQ 号或敏感词，请使用站内沟通。');
      return;
    }
    const res = await fetch(`${API_BASE}/messages/${activeThread.id}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ sender: currentUser.nickname, content })
    });
    if (!res.ok) {
      setError(await readError(res));
      return;
    }
    setDraft('');
    onReload();
  }

  return (
    <section className="page section">
      <SectionTitle icon={<MessageCircle />} title="站内私信" helper="围绕交易帖发起会话，禁止手机号线下联系" />
      {threads.length === 0 ? <EmptyState title="还没有私信会话" helper="在帖子详情里点击“私信发布者”即可创建会话。" /> : (
        <div className="messageLayout">
          <div className="threadList">
            {threads.map((thread) => {
              const unread = thread.unreadCount || 0;
              return <button type="button" className={activeThread?.id === thread.id ? 'active' : ''} key={thread.id} onClick={() => setActiveId(thread.id)}><strong>{thread.peer}</strong><span>{thread.postTitle}</span>{unread > 0 && <em>{unread}</em>}</button>;
            })}
          </div>
          {activeThread && <div className="conversation">
            <div className="conversationHeader"><strong>{activeThread.peer}</strong><span>{activeThread.postTitle}</span></div>
            <div className="messageStream">
              {activeThread.messages.map((message) => <div className={message.sender === currentUser.nickname ? 'message mine' : 'message'} key={message.id}><strong>{message.sender}</strong><p>{message.content}</p><span>{formatTime(message.createdAt)}</span></div>)}
            </div>
            <form className="messageComposer" onSubmit={sendMessage}>
              <input value={draft} onChange={(event) => setDraft(event.target.value)} placeholder="输入站内私信内容，禁止手机号" />
              <button type="submit">发送</button>
            </form>
            {error && <p className="formNote">{error}</p>}
          </div>}
        </div>
      )}
    </section>
  );
}

function DetailModal({ detail, currentUser, favoriteIds, sentIntents, onFavorite, onReport, onMessage, onTradeIntent, onMomentChanged, onClose }: { detail: { type: 'post' | 'moment' | 'pet'; item: MarketPost | Moment | Pet }; currentUser: UserProfile | null; favoriteIds: Set<number>; sentIntents: TradeIntent[]; onFavorite: (post: MarketPost) => void; onReport: (type: 'post' | 'moment', id: number) => void; onMessage: (post: MarketPost) => void; onTradeIntent: (post: MarketPost, message: string) => Promise<boolean>; onMomentChanged: () => void; onClose: () => void }) {
  const item = detail.item;
  const title = detail.type === 'post' ? (item as MarketPost).title : detail.type === 'moment' ? `${(item as Moment).petName} 的日常` : (item as Pet).name;
  const post = detail.type === 'post' ? item as MarketPost : null;
  const images = imageList(item as MarketPost | Moment | Pet);
  const existingIntent = post ? sentIntents.find((intent) => intent.postId === post.id) : undefined;
  const meta = detail.type === 'post'
    ? [(item as MarketPost).type, (item as MarketPost).status || '在售', formatPrice((item as MarketPost).price)]
    : detail.type === 'moment'
      ? [(item as Moment).category || '日常', `${(item as Moment).likes || 0} 次点赞`]
      : [(item as Pet).category, (item as Pet).status, formatPrice((item as Pet).price)];
  return (
    <div className="modalBackdrop" onClick={onClose}>
      <article className="detailModal" onClick={(event) => event.stopPropagation()}>
        <button type="button" className="closeButton" onClick={onClose}><X size={18} /></button>
        <div className="detailHeader">
          <h2>{title}</h2>
          <div className="detailMeta">{meta.filter(Boolean).map((value) => <span key={value}>{value}</span>)}</div>
        </div>
        {images.length > 0 && <div className="detailGallery">{images.map((url, index) => <img src={url} alt={`${title} 图片 ${index + 1}`} key={url} />)}</div>}
        {detail.type === 'post' && <DetailRows rows={[
          ['类型', (item as MarketPost).type],
          ['状态', (item as MarketPost).status || '在售'],
          ['价格', formatPrice((item as MarketPost).price)],
          ['分类', (item as MarketPost).category],
          ['地区', (item as MarketPost).city],
          ['发布人', (item as MarketPost).author],
          ['联系', (item as MarketPost).contact || CONTACT_VALUE],
          ['描述', (item as MarketPost).description]
        ]} />}
        {post && <div className="detailActions">
          <button type="button" className={favoriteIds.has(post.id) ? 'messageAction favoriteActive' : 'messageAction'} onClick={() => onFavorite(post)}><Heart size={18} />{favoriteIds.has(post.id) ? '已收藏' : '收藏帖子'}</button>
          <button type="button" className="messageAction" onClick={() => onMessage(post)}><MessageCircle size={18} />私信发布者</button>
          <button type="button" className="reportAction" onClick={() => onReport('post', post.id)}><Flag size={18} />举报</button>
        </div>}
        {post && <TradeIntentForm post={post} currentUser={currentUser} existingIntent={existingIntent} onSubmit={onTradeIntent} />}
        {detail.type === 'moment' && <DetailRows rows={[
          ['分类', (item as Moment).category || '日常'],
          ['地区', (item as Moment).city || '未选择地区'],
          ['宠物', (item as Moment).petName],
          ['作者', (item as Moment).author],
          ['点赞', String((item as Moment).likes)],
          ['内容', (item as Moment).content]
        ]} />}
        {detail.type === 'moment' && <div className="detailActions">
          <button type="button" className="reportAction" onClick={() => onReport('moment', (item as Moment).id)}><Flag size={18} />举报</button>
        </div>}
        {detail.type === 'moment' && <MomentInteraction moment={item as Moment} currentUser={currentUser} onChanged={onMomentChanged} />}
        {detail.type === 'pet' && <DetailRows rows={[
          ['分类', (item as Pet).category],
          ['品种', (item as Pet).breed],
          ['年龄', (item as Pet).age],
          ['年龄段', (item as Pet).ageRange || '未填写'],
          ['性别', (item as Pet).gender || '未知'],
          ['地区', (item as Pet).city],
          ['状态', (item as Pet).status],
          ['价格', formatPrice((item as Pet).price)],
          ['健康', (item as Pet).healthInfo],
          ['健康记录', petHealthTags(item as Pet).join('、') || '未填写'],
          ['发布者', (item as Pet).ownerName || '平台示例用户'],
          ['性格', (item as Pet).personality],
          ['照护说明', (item as Pet).careNotes || '暂无补充说明']
        ]} />}
      </article>
    </div>
  );
}

function TradeIntentForm({ post, currentUser, existingIntent, onSubmit }: { post: MarketPost; currentUser: UserProfile | null; existingIntent?: TradeIntent; onSubmit: (post: MarketPost, message: string) => Promise<boolean> }) {
  const [message, setMessage] = React.useState('');
  const [busy, setBusy] = React.useState(false);
  const [notice, setNotice] = React.useState('');
  const isOwner = currentUser?.nickname === post.author;

  async function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const text = message.trim();
    setNotice('');
    if (!currentUser) return setNotice('请先登录后再提交交易意向。');
    if (isOwner) return setNotice('这是你自己发布的帖子，不能提交意向。');
    if (existingIntent) return setNotice(`你已经提交过意向，当前状态：${existingIntent.status}`);
    if (!text) return setNotice('请简单说明你的交易意向。');
    if (hasUnsafeContent(text)) return setNotice('意向说明不能填写手机号、微信号、QQ 号或敏感词。');
    setBusy(true);
    const ok = await onSubmit(post, text);
    setBusy(false);
    if (ok) {
      setMessage('');
      setNotice('意向已提交，发布者可在“我的发布”里处理。');
    }
  }

  return (
    <form className="intentForm" onSubmit={submit}>
      <div className="intentFormTitle"><Clock3 size={17} /><strong>提交交易意向</strong><span>站内留痕，后续可处理状态</span></div>
      {existingIntent ? <p className="formNote">你已提交过该帖子意向，当前状态：{existingIntent.status}</p> : (
        <>
          <textarea value={message} onChange={(event) => setMessage(event.target.value)} disabled={!currentUser || isOwner || busy} placeholder="说明你想购买、互换、领养或预约看宠的意向，禁止手机号线下联系。" />
          <button type="submit" disabled={!currentUser || isOwner || busy}>{busy ? '提交中...' : '提交意向'}</button>
        </>
      )}
      {notice && <p className="formNote">{notice}</p>}
    </form>
  );
}

function MomentInteraction({ moment, currentUser, onChanged }: { moment: Moment; currentUser: UserProfile | null; onChanged: () => void }) {
  const [comments, setComments] = React.useState<MomentComment[]>([]);
  const [content, setContent] = React.useState('');
  const [error, setError] = React.useState('');
  const [likes, setLikes] = React.useState(moment.likes || 0);

  const loadComments = React.useCallback(() => {
    fetch(`${API_BASE}/moments/${moment.id}/comments`)
      .then((res) => res.ok ? res.json() : Promise.reject())
      .then(setComments)
      .catch(() => setComments([]));
  }, [moment.id]);

  React.useEffect(loadComments, [loadComments]);

  async function likeMoment() {
    const res = await fetch(`${API_BASE}/moments/${moment.id}/like`, { method: 'POST' });
    if (!res.ok) return setError(await readError(res));
    const next = await res.json() as Moment;
    setLikes(next.likes || 0);
    onChanged();
  }

  async function submitComment(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const text = content.trim();
    setError('');
    if (!currentUser) return setError('请先登录后再评论。');
    if (!text) return setError('请输入评论内容。');
    if (hasUnsafeContent(text)) return setError('评论不能填写手机号、微信号、QQ 号或敏感词。');
    const res = await fetch(`${API_BASE}/moments/${moment.id}/comments`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ author: currentUser.nickname, content: text })
    });
    if (!res.ok) return setError(await readError(res));
    setContent('');
    loadComments();
  }

  async function shareMoment() {
    const text = `${moment.author} 分享了 ${moment.petName} 的日常：${moment.content}`;
    try {
      await navigator.clipboard.writeText(text);
      setError('日常分享文案已复制');
    } catch {
      setError(text);
    }
  }

  return (
    <div className="momentInteraction">
      <div className="interactionActions">
        <button type="button" onClick={likeMoment}><Heart size={16} />点赞 {likes}</button>
        <button type="button" onClick={shareMoment}><MessageCircle size={16} />分享日常</button>
      </div>
      <div className="commentPanel">
        <h3>评论区</h3>
        {comments.length ? comments.map((comment) => <div className="commentItem" key={comment.id}><strong>{comment.author}</strong><p>{comment.content}</p><span>{formatTime(comment.createdAt)}</span></div>) : <p className="emptyState">还没有评论，来聊聊这条日常吧。</p>}
      </div>
      <form className="commentComposer" onSubmit={submitComment}>
        <input value={content} onChange={(event) => setContent(event.target.value)} placeholder="写评论，禁止手机号、微信号或 QQ 号" />
        <button type="submit">评论</button>
      </form>
      {error && <p className="formNote">{error}</p>}
    </div>
  );
}

function DetailRows({ rows }: { rows: Array<[string, string]> }) {
  return <dl className="detailRows">{rows.map(([key, value]) => <React.Fragment key={key}><dt>{key}</dt><dd>{value}</dd></React.Fragment>)}</dl>;
}

function EmptyState({ title, helper }: { title: string; helper: string }) {
  return <div className="emptyBlock"><PawPrint size={26} /><strong>{title}</strong><span>{helper}</span></div>;
}

function LoadingState({ label }: { label: string }) {
  return <div className="loadingBlock"><span /> <strong>{label}</strong></div>;
}

function InlineError({ message }: { message: string }) {
  return <p className="formError"><Flag size={16} />{message}</p>;
}

function matchesCategory(filter: string, category?: string) {
  return filter === '全部' || category === filter;
}

function matchesCity(filter: string, city?: string) {
  return filter === '全部' || (city || '').includes(filter);
}

function matchesType(filter: string, type?: string) {
  return filter === '全部' || type === filter;
}

function matchesPrice(minPrice: string, maxPrice: string, value?: number) {
  const price = Number(value || 0);
  const min = minPrice === '' ? null : Number(minPrice);
  const max = maxPrice === '' ? null : Number(maxPrice);
  if (min !== null && !Number.isNaN(min) && price < min) return false;
  if (max !== null && !Number.isNaN(max) && price > max) return false;
  return true;
}

function matchesText(query: string, fields: Array<string | undefined>) {
  const keyword = query.trim().toLowerCase();
  if (!keyword) return true;
  return fields.some((field) => (field || '').toLowerCase().includes(keyword));
}

function hasOffsiteContact(value: string) {
  return phonePattern.test(value) || offsiteContactPattern.test(value);
}

function hasSensitiveWord(value: string) {
  const text = value.toLowerCase();
  return sensitiveWords.some((word) => text.includes(word.toLowerCase()));
}

function hasUnsafeContent(value: string) {
  return hasOffsiteContact(value) || hasSensitiveWord(value);
}

function withTradeDetails(description: string, fields: Array<{ name: string; label: string }>, body: Record<string, FormDataEntryValue>) {
  const details = fields
    .map((field) => [field.label, String(body[field.name] || '').trim()] as const)
    .filter(([, value]) => value.length > 0);
  if (!details.length) return description;
  const extra = details.map(([label, value]) => `${label}：${value}`).join('\n');
  return `${description.trim()}\n\n交易补充：\n${extra}`;
}

function sortByTime<T extends { createdAt?: string }>(items: T[], mode: 'latest' | 'oldest') {
  return [...items].sort((left, right) => {
    const leftTime = left.createdAt ? new Date(left.createdAt).getTime() : 0;
    const rightTime = right.createdAt ? new Date(right.createdAt).getTime() : 0;
    return mode === 'latest' ? rightTime - leftTime : leftTime - rightTime;
  });
}

function cityOptions(regions: Region[]) {
  return Array.from(new Set(regions.flatMap((province) => province.cities.map((city) => city.name))));
}

function formatTime(value: string) {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '';
  return date.toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' });
}

function statusClass(status: string) {
  if (status === '已同意') return 'accepted';
  if (status === '已拒绝') return 'rejected';
  if (status === '已取消') return 'canceled';
  return 'pending';
}

function AuditBadge({ value }: { value?: string }) {
  const status = value || '审核通过';
  if (status === '审核通过') return null;
  return <span className={status === '已下架' ? 'auditBadge removed' : 'auditBadge'}>{status}</span>;
}

function isSuperAdmin(user: UserProfile | null) {
  return user?.role === 'SUPER_ADMIN';
}

function maskPhone(value?: string) {
  return value ? value.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2') : '';
}

function formatPrice(value?: number) {
  return value && value > 0 ? `￥${value}` : '面议';
}

function imageList(item: { imageUrl?: string; imageUrls?: string }) {
  const urls = (item.imageUrls || item.imageUrl || '')
    .split(',')
    .map((url) => url.trim())
    .filter(Boolean);
  return Array.from(new Set(urls));
}

function primaryImage(item: { imageUrl?: string; imageUrls?: string }) {
  return imageList(item)[0] || '';
}

function petHealthTags(pet: Pet) {
  const tags = (pet.healthRecords || pet.healthInfo || '')
    .split(/[,，、]/)
    .map((tag) => tag.trim())
    .filter(Boolean);
  if (pet.vaccinated) tags.push('已疫苗');
  if (pet.dewormed) tags.push('已驱虫');
  if (pet.neutered) tags.push('已绝育');
  return Array.from(new Set(tags));
}

function validateImage(file: File) {
  if (!allowedImageTypes.includes(file.type)) {
    return '图片格式不支持，请上传 JPG、PNG、WebP 或 GIF。';
  }
  if (file.size > MAX_IMAGE_SIZE) {
    return '图片不能超过 5MB，请压缩后再上传。';
  }
  return '';
}

function parseRegion(value: string | undefined, regions: Region[]) {
  const [provinceName, cityName, districtName] = (value || '').split(/\s+/);
  const province = regions.find((item) => item.name === provinceName) || regions[0];
  const city = province.cities.find((item) => item.name === cityName) || province.cities[0];
  const district = city.districts.includes(districtName) ? districtName : city.districts[0];
  return { province: province.name, city: city.name, district };
}

async function readError(res: Response) {
  try {
    const data = await res.json();
    return data.message || data.error || '请求失败，请检查填写内容。';
  } catch {
    return '请求失败，请检查后端服务是否正常运行。';
  }
}

function RegionPicker(props: {
  province: string; city: string; district: string; selectedProvince: Region; selectedCity: Region['cities'][number]; regions: Region[]; disabled: boolean; onProvince: (value: string) => void; onCity: (value: string) => void; onDistrict: (value: string) => void;
}) {
  return (
    <div className="regionGrid">
      <select value={props.province} disabled={props.disabled} onChange={(event) => props.onProvince(event.target.value)}>{props.regions.map((item) => <option key={item.name}>{item.name}</option>)}</select>
      <select value={props.city} disabled={props.disabled} onChange={(event) => props.onCity(event.target.value)}>{props.selectedProvince.cities.map((item) => <option key={item.name}>{item.name}</option>)}</select>
      <select value={props.district} disabled={props.disabled} onChange={(event) => props.onDistrict(event.target.value)}>{props.selectedCity.districts.map((item) => <option key={item}>{item}</option>)}</select>
    </div>
  );
}

function Metric({ value, label }: { value: number; label: string }) {
  return <div className="metric"><strong>{value}</strong><span>{label}</span></div>;
}

function SectionTitle({ icon, title, helper }: { icon: React.ReactNode; title: string; helper: string }) {
  return <div className="sectionTitle"><div>{icon}</div><div><h2>{title}</h2><p>{helper}</p></div></div>;
}

function placeholder(label: string) {
  return <div className="placeholder"><PawPrint size={34} /><span>{label}</span></div>;
}

function imageBox(url: string, label: string) {
  return url ? <img className="cover" src={url} alt={label} /> : placeholder(label);
}

ReactDOM.createRoot(document.getElementById('root')!).render(<App />);
