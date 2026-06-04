import { matchesCity } from './region';

const cityFilters = [
  { code: '', name: 'All' },
  { code: '310000', name: 'Shanghai Shanghai' },
  { code: '330100', name: 'Zhejiang Hangzhou' }
];

describe('region helpers', () => {
  it('matches by cityCode when it is available', () => {
    expect(matchesCity('330100', '330100', 'Zhejiang Hangzhou Xihu', cityFilters)).toBe(true);
    expect(matchesCity('310000', '330100', 'Zhejiang Hangzhou Xihu', cityFilters)).toBe(false);
  });

  it('falls back to legacy city text when cityCode is missing', () => {
    expect(matchesCity('330100', undefined, 'Zhejiang Hangzhou Xihu', cityFilters)).toBe(true);
    expect(matchesCity('310000', undefined, 'Zhejiang Hangzhou Xihu', cityFilters)).toBe(false);
  });
});
