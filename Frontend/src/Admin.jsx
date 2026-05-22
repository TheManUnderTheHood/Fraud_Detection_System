import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ShieldAlert, LogOut, CheckCircle, RefreshCw, Activity, AlertOctagon, CheckSquare } from 'lucide-react';
import { toast } from 'react-toastify';
import api from './api';

export default function Admin() {
    const [alerts, setAlerts] = useState([]);
    const [stats, setStats] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        fetchAlertsAndStats();
    }, []);

    const fetchAlertsAndStats = async () => {
        try {
            const alertRes = await api.get('/admin/alerts');
            setAlerts(alertRes.data);

            const statsRes = await api.get('/admin/statistics');
            setStats(statsRes.data);
        } catch (err) {
            if (err.response?.status === 403) {
                toast.error("Unauthorized! You are not an Admin.");
            } else {
                toast.error("Failed to load admin data. Please try again.");
            }
            navigate('/dashboard');
        }
    };

    const reviewAlert = async (alertId) => {
        try {
            await api.put(`/admin/alerts/${alertId}/review`);
            toast.success(`Alert #${alertId} resolved!`);
            fetchAlertsAndStats(); // refresh everything
        } catch {
            toast.error("Failed to review alert.");
        }
    };

    const logout = () => {
        localStorage.removeItem('jwt');
        navigate('/');
    };

    return (
        <div className="min-h-screen bg-slate-50 p-6">
            <div className="max-w-6xl mx-auto">
                <div className="flex justify-between items-center mb-8 bg-white p-4 rounded-xl shadow-xs border border-red-100">
                    <h1 className="text-2xl font-bold text-red-600 flex items-center gap-2">
                        <ShieldAlert className="w-8 h-8" /> AML Compliance Center
                    </h1>
                    <div className="flex gap-4">
                        <button onClick={fetchAlertsAndStats} className="text-slate-600 hover:bg-slate-100 px-4 py-2 rounded-lg font-medium flex items-center gap-2 transition">
                            <RefreshCw className="w-4 h-4" /> Refresh
                        </button>
                        <button onClick={logout} className="text-slate-600 hover:bg-slate-100 px-4 py-2 rounded-lg font-medium flex items-center gap-2 transition">
                            <LogOut className="w-4 h-4" /> Logout
                        </button>
                    </div>
                </div>

                {/* KPI METRICS ROW */}
                {stats && (
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
                        <div className="bg-white p-6 rounded-2xl shadow-xs border border-slate-100 flex items-center gap-4">
                            <div className="bg-blue-100 p-4 rounded-full text-blue-600"><Activity className="w-6 h-6"/></div>
                            <div>
                                <p className="text-slate-500 text-sm font-medium">Total Global TXs</p>
                                <h3 className="text-2xl font-bold text-slate-800">{stats.totalTransactions}</h3>
                            </div>
                        </div>
                        <div className="bg-white p-6 rounded-2xl shadow-xs border border-red-100 flex items-center gap-4">
                            <div className="bg-red-100 p-4 rounded-full text-red-600"><AlertOctagon className="w-6 h-6"/></div>
                            <div>
                                <p className="text-slate-500 text-sm font-medium">Pending Alerts</p>
                                <h3 className="text-2xl font-bold text-red-600">{stats.pendingAlerts}</h3>
                            </div>
                        </div>
                        <div className="bg-white p-6 rounded-2xl shadow-xs border border-emerald-100 flex items-center gap-4">
                            <div className="bg-emerald-100 p-4 rounded-full text-emerald-600"><CheckSquare className="w-6 h-6"/></div>
                            <div>
                                <p className="text-slate-500 text-sm font-medium">Reviewed & Cleared</p>
                                <h3 className="text-2xl font-bold text-emerald-600">{stats.reviewedAlerts}</h3>
                            </div>
                        </div>
                    </div>
                )}

                <div className="bg-white rounded-2xl shadow-xs border border-red-200 overflow-hidden">
                    <div className="bg-red-50 p-4 border-b border-red-100 flex justify-between items-center">
                        <h3 className="font-bold text-red-800">Action Required: Fraud Alerts</h3>
                        <span className="bg-red-600 text-white text-xs px-2.5 py-1 rounded-full font-bold">{alerts.length} Pending</span>
                    </div>
                    <div className="overflow-x-auto">
                        <table className="w-full text-left border-collapse">
                            <thead>
                                <tr className="bg-white text-slate-500 text-sm border-b border-slate-100">
                                    <th className="p-4 font-medium">Alert ID</th>
                                    <th className="p-4 font-medium">Date</th>
                                    <th className="p-4 font-medium">TX Ref</th>
                                    <th className="p-4 font-medium">Triggered Rule</th>
                                    <th className="p-4 font-medium text-right">Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                {alerts.length === 0 ? (
                                    <tr>
                                        <td colSpan="5" className="p-12 text-center">
                                            <CheckCircle className="w-12 h-12 text-emerald-400 mx-auto mb-3" />
                                            <p className="text-slate-500 font-medium">All clean! System is operating normally.</p>
                                        </td>
                                    </tr>
                                ) : alerts.map((alert) => (
                                    <tr key={alert.id} className="border-b border-slate-50 hover:bg-slate-50 transition">
                                        <td className="p-4 text-red-600 font-bold">#{alert.id}</td>
                                        <td className="p-4 text-sm text-slate-500">{new Date(alert.createdAt).toLocaleString()}</td>
                                        <td className="p-4 font-mono text-sm text-slate-600">TX-{alert.transactionId ?? alert.transaction?.id}</td>
                                        <td className="p-4">
                                            <span className="bg-red-100 text-red-700 px-3 py-1.5 rounded-lg text-xs font-bold border border-red-200">
                                                {alert.reason}
                                            </span>
                                        </td>
                                        <td className="p-4 text-right">
                                            <button onClick={() => reviewAlert(alert.id)} className="bg-white border border-emerald-500 text-emerald-600 hover:bg-emerald-50 px-4 py-1.5 rounded-lg text-sm font-semibold transition shadow-sm">
                                                Mark Safe
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    );
}