import React, { useState } from 'react';
import { TrendingUp, ArrowUpRight, ArrowDownRight, ShieldCheck, Zap } from 'lucide-react';

interface PricePoint {
  timeLabel: string;
  rateNgn: number;
}

interface PriceTrendProps {
  cardName?: string;
  currentRate?: number;
}

export const PriceTrendVisualization: React.FC<PriceTrendProps> = ({
  cardName = 'Apple & iTunes Gift Card',
  currentRate = 1430,
}) => {
  const [timeframe, setTimeframe] = useState<'24H' | '7D' | '30D' | '1Y'>('7D');
  const [selectedCategory, setSelectedCategory] = useState<string>('Gaming');

  const historyPoints: Record<string, PricePoint[]> = {
    '24H': [
      { timeLabel: '00:00', rateNgn: currentRate - 18 },
      { timeLabel: '04:00', rateNgn: currentRate - 12 },
      { timeLabel: '08:00', rateNgn: currentRate - 5 },
      { timeLabel: '12:00', rateNgn: currentRate + 8 },
      { timeLabel: '16:00', rateNgn: currentRate + 4 },
      { timeLabel: '20:00', rateNgn: currentRate + 15 },
      { timeLabel: 'Now', rateNgn: currentRate },
    ],
    '7D': [
      { timeLabel: 'Mon', rateNgn: currentRate - 45 },
      { timeLabel: 'Tue', rateNgn: currentRate - 28 },
      { timeLabel: 'Wed', rateNgn: currentRate - 35 },
      { timeLabel: 'Thu', rateNgn: currentRate - 10 },
      { timeLabel: 'Fri', rateNgn: currentRate + 12 },
      { timeLabel: 'Sat', rateNgn: currentRate + 22 },
      { timeLabel: 'Sun', rateNgn: currentRate },
    ],
    '30D': [
      { timeLabel: 'Week 1', rateNgn: currentRate - 110 },
      { timeLabel: 'Week 2', rateNgn: currentRate - 75 },
      { timeLabel: 'Week 3', rateNgn: currentRate - 30 },
      { timeLabel: 'Week 4', rateNgn: currentRate + 15 },
      { timeLabel: 'Today', rateNgn: currentRate },
    ],
    '1Y': [
      { timeLabel: 'Q1', rateNgn: currentRate - 240 },
      { timeLabel: 'Q2', rateNgn: currentRate - 160 },
      { timeLabel: 'Q3', rateNgn: currentRate - 80 },
      { timeLabel: 'Q4', rateNgn: currentRate },
    ],
  };

  const points = historyPoints[timeframe] || historyPoints['7D'];
  const minRate = Math.min(...points.map((p) => p.rateNgn)) - 10;
  const maxRate = Math.max(...points.map((p) => p.rateNgn)) + 10;
  const range = Math.max(maxRate - minRate, 1);

  const first = points[0]?.rateNgn || currentRate;
  const last = points[points.length - 1]?.rateNgn || currentRate;
  const changePct = (((last - first) / first) * 100).toFixed(1);
  const isSurge = Number(changePct) >= 0;

  return (
    <div className="bg-slate-900 border border-emerald-800/40 rounded-2xl p-6 text-white shadow-xl">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-800">
        <div>
          <div className="flex items-center gap-2">
            <span className="p-2 rounded-lg bg-emerald-500/10 text-emerald-400">
              <TrendingUp className="w-5 h-5" />
            </span>
            <h3 className="text-lg font-bold">{cardName}</h3>
          </div>
          <p className="text-xs text-slate-400 mt-1">Live exchange rate movement against Nigerian Naira (NGN)</p>
        </div>

        <div className="flex items-center gap-3">
          <div className="text-right">
            <div className="text-2xl font-black text-emerald-400">₦{currentRate.toLocaleString()}/$</div>
            <div className={`text-xs font-semibold flex items-center justify-end gap-1 ${isSurge ? 'text-emerald-400' : 'text-rose-400'}`}>
              {isSurge ? <ArrowUpRight className="w-3.5 h-3.5" /> : <ArrowDownRight className="w-3.5 h-3.5" />}
              {isSurge ? `+${changePct}%` : `${changePct}%`} ({timeframe})
            </div>
          </div>
        </div>
      </div>

      {/* Category and Timeframe selectors */}
      <div className="flex flex-wrap items-center justify-between gap-2 mt-4">
        <div className="flex items-center gap-1.5 bg-slate-800/80 p-1 rounded-xl">
          {['Gaming', 'Retail', 'Digital', 'Subscriptions'].map((cat) => (
            <button
              key={cat}
              onClick={() => setSelectedCategory(cat)}
              className={`px-3 py-1 text-xs font-medium rounded-lg transition-all ${
                selectedCategory === cat ? 'bg-emerald-600 text-white font-bold' : 'text-slate-400 hover:text-white'
              }`}
            >
              {cat}
            </button>
          ))}
        </div>

        <div className="flex items-center gap-1 bg-slate-800/80 p-1 rounded-xl">
          {(['24H', '7D', '30D', '1Y'] as const).map((tf) => (
            <button
              key={tf}
              onClick={() => setTimeframe(tf)}
              className={`px-3 py-1 text-xs font-medium rounded-lg transition-all ${
                timeframe === tf ? 'bg-emerald-500 text-slate-950 font-bold' : 'text-slate-400 hover:text-white'
              }`}
            >
              {tf}
            </button>
          ))}
        </div>
      </div>

      {/* SVG Interactive Bezier Trend Chart */}
      <div className="relative mt-6 h-48 w-full">
        <svg viewBox="0 0 500 160" className="w-full h-full overflow-visible">
          <defs>
            <linearGradient id="emeraldGradient" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor="#10b981" stopOpacity="0.4" />
              <stop offset="100%" stopColor="#10b981" stopOpacity="0.0" />
            </linearGradient>
          </defs>

          {/* Area fill */}
          <path
            d={`
              M 0 160
              ${points
                .map((pt, i) => {
                  const x = (i / (points.length - 1)) * 500;
                  const y = 140 - ((pt.rateNgn - minRate) / range) * 120;
                  return `L ${x} ${y}`;
                })
                .join(' ')}
              L 500 160 Z
            `}
            fill="url(#emeraldGradient)"
          />

          {/* Trend Line */}
          <path
            d={`
              ${points
                .map((pt, i) => {
                  const x = (i / (points.length - 1)) * 500;
                  const y = 140 - ((pt.rateNgn - minRate) / range) * 120;
                  return `${i === 0 ? 'M' : 'L'} ${x} ${y}`;
                })
                .join(' ')}
            `}
            fill="none"
            stroke="#10b981"
            strokeWidth="3"
            strokeLinecap="round"
          />

          {/* Points */}
          {points.map((pt, i) => {
            const x = (i / (points.length - 1)) * 500;
            const y = 140 - ((pt.rateNgn - minRate) / range) * 120;
            const isLast = i === points.length - 1;
            return (
              <g key={i}>
                <circle cx={x} cy={y} r={isLast ? '6' : '3.5'} fill={isLast ? '#fbbf24' : '#10b981'} stroke="#022c22" strokeWidth="2" />
              </g>
            );
          })}
        </svg>
      </div>

      {/* X-axis labels */}
      <div className="flex justify-between items-center text-xs text-slate-400 mt-2 px-1">
        {points.map((p, idx) => (
          <div key={idx} className="text-center">
            <div>{p.timeLabel}</div>
            <div className="text-[10px] text-emerald-400 font-semibold">₦{Math.round(p.rateNgn)}</div>
          </div>
        ))}
      </div>

      {/* Safety Notice Footer */}
      <div className="mt-6 flex items-center justify-between text-xs text-slate-400 bg-slate-800/40 px-3.5 py-2.5 rounded-xl">
        <div className="flex items-center gap-1.5">
          <ShieldCheck className="w-4 h-4 text-emerald-400" />
          <span>Rates are guaranteed upon trade submission.</span>
        </div>
        <div className="flex items-center gap-1 text-emerald-400 font-medium">
          <Zap className="w-3.5 h-3.5" />
          <span>Updates in real-time</span>
        </div>
      </div>
    </div>
  );
};
