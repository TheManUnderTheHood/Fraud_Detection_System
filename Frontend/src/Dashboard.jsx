import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Wallet, Send, History, AlertTriangle, LogOut, CheckCircle, Loader2, BarChart3 } from 'lucide-react';
import { toast } from 'react-toastify';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import api from './api';

export default function Dashboard() {
    const [account, setAccount] = useState(null);
    const [history, setHistory] = useState([]);
    const [transferData, setTransferData] = useState({ receiverAccountId: '', amount: '' });
    const [isTransferring, setIsTransferring] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
        fetchAccountData();
    }, []);

    const fetchAccountData = async () => {
        try {
            const res = await api.get('/account/my-accounts');
            if (res.data.length > 0) {
                setAccount(res.data[0]);
                fetchHistory(res.data[0].id);
            }
        } catch {
            navigate('/');
        }
    };

    const fetchHistory = async (accountId) => {
        try {
            const res = await api.get(`/transaction/history/${accountId}`);
            setHistory(res.data.sort((a, b) => b.id - a.id));
        } catch (err) {
            toast.error(err.response?.data?.error || "Failed to load transaction history.");
            setHistory([]);
        }
    };

    const handleTransfer = async (e) => {
        e.preventDefault();
        setIsTransferring(true);
        try {
            const res = await api.post('/transaction/transfer', {
                senderAccountId: account.id,
                receiverAccountId: parseInt(transferData.receiverAccountId),
                amount: parseFloat(transferData.amount)
            });

            if (res.data.flagged) {
                toast.error("Transfer completed, but flagged by AML Rules!");
            } else {
                toast.success("Transfer successful!");
            }
            setTransferData({ receiverAccountId: '', amount: '' });
            fetchAccountData();
        } catch (err) {
            toast.error(err.response?.data?.error || "Transfer failed.");
        } finally {
            setIsTransferring(false);
        }
    };

    const logout = () => {
        localStorage.removeItem('jwt');
        navigate('/');
    };

    // Prepare data for the chart (Last 7 transactions)
    const chartData = history.slice(0, 7).reverse().map(tx => ({
        name: `TX-${tx.id}`,
        Sent: tx.senderAccount.id === account?.id ? tx.amount : 0,
        Received: tx.receiverAccount.id === account?.id ? tx.amount : 0,
    }));

    return (
        <div className="min-h-screen bg-slate-50 p-6">
            <div className="max-w-6xl mx-auto">
                <div className="flex justify-between items-center mb-8 bg-white p-4 rounded-xl shadow-xs border border-slate-100">
                    <h1 className="text-2xl font-bold text-slate-800 flex items-center gap-2">
                        <Wallet className="text-blue-600" /> My Banking Dashboard
                    </h1>
                    <button onClick={logout} className="text-red-600 hover:bg-red-50 px-4 py-2 rounded-lg font-medium flex items-center gap-2 transition">
                        <LogOut className="w-4 h-4" /> Logout
                    </button>
                </div>

                <div className="grid grid-cols-1 xl:grid-cols-3 gap-6 mb-6">
                    {/* Left Column: Balance & Transfer */}
                    <div className="space-y-6">
                        {account ? (
                            <div className="bg-gradient-to-br from-blue-600 to-indigo-700 rounded-2xl p-6 text-white shadow-lg">
                                <p className="text-blue-100 text-sm font-medium mb-1">Available Balance</p>
                                <h2 className="text-4xl font-bold mb-6">${account.balance.toLocaleString(undefined, {minimumFractionDigits: 2})}</h2>
                                <div className="bg-white/20 px-4 py-2 rounded-lg flex justify-between items-center backdrop-blur-sm">
                                    <span className="text-sm">Account ID</span>
                                    <span className="font-mono font-bold text-lg">#{account.id}</span>
                                </div>
                            </div>
                        ) : (
                            <div className="bg-white rounded-2xl p-8 text-center shadow-xs border border-dashed border-slate-300">
                                <p className="text-slate-500 mb-4">You don't have a bank account yet.</p>
                                <button onClick={async () => { await api.post('/account/create'); fetchAccountData(); }} className="bg-blue-600 text-white px-6 py-2 rounded-lg font-medium hover:bg-blue-700 transition">
                                    Open Account ($10k Bonus)
                                </button>
                            </div>
                        )}

                        {account && (
                            <div className="bg-white rounded-2xl p-6 shadow-xs border border-slate-100">
                                <h3 className="text-lg font-bold text-slate-800 mb-4 flex items-center gap-2"><Send className="w-5 h-5 text-blue-600"/> Send Money</h3>
                                <form onSubmit={handleTransfer} className="space-y-4">
                                    <div>
                                        <label className="block text-sm text-slate-600 mb-1">Receiver Account ID</label>
                                        <input type="number" required value={transferData.receiverAccountId} onChange={e => setTransferData({...transferData, receiverAccountId: e.target.value})} className="w-full px-4 py-2 bg-slate-50 border border-slate-200 rounded-lg outline-hidden focus:ring-2 focus:ring-blue-500" placeholder="e.g. 2"/>
                                    </div>
                                    <div>
                                        <label className="block text-sm text-slate-600 mb-1">Amount ($)</label>
                                        <input type="number" step="0.01" required value={transferData.amount} onChange={e => setTransferData({...transferData, amount: e.target.value})} className="w-full px-4 py-2 bg-slate-50 border border-slate-200 rounded-lg outline-hidden focus:ring-2 focus:ring-blue-500" placeholder="100.00"/>
                                    </div>
                                    <button type="submit" disabled={isTransferring} className="w-full bg-blue-600 text-white py-2.5 rounded-lg font-medium hover:bg-blue-700 transition flex items-center justify-center gap-2 disabled:bg-blue-400">
                                        {isTransferring ? <Loader2 className="w-5 h-5 animate-spin"/> : 'Transfer Funds'}
                                    </button>
                                </form>
                            </div>
                        )}
                    </div>

                    {/* Right Column: Chart & History */}
                    <div className="xl:col-span-2 space-y-6 min-w-0">
                        {/* CHART PANEL */}
                        {account && history.length > 0 && (
                            <div className="bg-white rounded-2xl shadow-xs border border-slate-100 p-6">
                                <h3 className="text-lg font-bold text-slate-800 mb-4 flex items-center gap-2"><BarChart3 className="w-5 h-5 text-indigo-500"/> Cash Flow (Last 7)</h3>
                                <div className="h-64 w-full min-w-0">
                                    <ResponsiveContainer width="100%" height="100%" minWidth={0}>
                                        <BarChart data={chartData}>
                                            <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0"/>
                                            <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{fill: '#64748b', fontSize: 12}} dy={10}/>
                                            <YAxis axisLine={false} tickLine={false} tick={{fill: '#64748b', fontSize: 12}} dx={-10}/>
                                            <Tooltip cursor={{fill: '#f8fafc'}} contentStyle={{borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)'}}/>
                                            <Bar dataKey="Received" fill="#10b981" radius={[4, 4, 0, 0]} barSize={30} />
                                            <Bar dataKey="Sent" fill="#ef4444" radius={[4, 4, 0, 0]} barSize={30} />
                                        </BarChart>
                                    </ResponsiveContainer>
                                </div>
                            </div>
                        )}

                        {/* HISTORY PANEL */}
                        <div className="bg-white rounded-2xl shadow-xs border border-slate-100 overflow-hidden">
                            <div className="p-6 border-b border-slate-100">
                                <h3 className="text-lg font-bold text-slate-800 flex items-center gap-2"><History className="w-5 h-5 text-slate-500"/> Recent Transactions</h3>
                            </div>
                            <div className="overflow-x-auto">
                                <table className="w-full text-left border-collapse">
                                    <thead>
                                        <tr className="bg-slate-50 text-slate-500 text-sm border-b border-slate-100">
                                            <th className="p-4 font-medium">TX ID</th>
                                            <th className="p-4 font-medium">Details</th>
                                            <th className="p-4 font-medium">Amount</th>
                                            <th className="p-4 font-medium">AML Status</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {history.length === 0 ? (
                                            <tr><td colSpan="4" className="p-8 text-center text-slate-500">No transactions found.</td></tr>
                                        ) : history.slice(0, 10).map((tx) => {
                                            const isSender = account && tx.senderAccount.id === account.id;
                                            return (
                                                <tr key={tx.id} className="border-b border-slate-50 hover:bg-slate-50 transition">
                                                    <td className="p-4 text-slate-600 text-sm">#{tx.id}</td>
                                                    <td className="p-4 text-sm">
                                                        {isSender ? <span className="text-slate-600">Sent to #{tx.receiverAccount.id}</span> : <span className="text-slate-600">Received from #{tx.senderAccount.id}</span>}
                                                    </td>
                                                    <td className={`p-4 font-bold ${isSender ? 'text-red-500' : 'text-emerald-500'}`}>
                                                        {isSender ? '-' : '+'}${tx.amount.toLocaleString(undefined, {minimumFractionDigits: 2})}
                                                    </td>
                                                    <td className="p-4">
                                                        {tx.flagged ? (
                                                            <span className="inline-flex items-center gap-1 bg-red-100 text-red-700 px-2.5 py-1 rounded-full text-xs font-semibold"><AlertTriangle className="w-3 h-3"/> Flagged</span>
                                                        ) : (
                                                            <span className="inline-flex items-center gap-1 bg-emerald-100 text-emerald-700 px-2.5 py-1 rounded-full text-xs font-semibold"><CheckCircle className="w-3 h-3"/> Clean</span>
                                                        )}
                                                    </td>
                                                </tr>
                                            );
                                        })}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}