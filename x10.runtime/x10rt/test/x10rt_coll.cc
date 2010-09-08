#include <cstdlib>
#include <cstdio>
#include <cstring>
#include <cmath>
#include <iostream>
#include <string>

#include <stdint.h>

#include <sched.h>

#include <x10rt_front.h>
#include <x10rt_ser.h>
#include <x10rt_net.h>
#include <x10rt_logical.h>


// {{{ nano_time
#include <sys/time.h>

static unsigned long long nano_time() {
    struct ::timeval tv;
    gettimeofday(&tv, NULL);
    return (unsigned long long)(tv.tv_sec * 1000000000ULL + tv.tv_usec * 1000ULL);
} // }}}

x10rt_msg_type TEST_ID, PRINT_ID, FINISH_ID;

int time_to_quit;

static void x10rt_barrier_b(x10rt_team t, x10rt_place r)
{
    int finished = 0;
    x10rt_barrier(t, r, x10rt_one_setter, &finished);
    while (!finished) x10rt_probe();
}

static void recv_finish(const x10rt_msg_params *p)
{
    x10rt_deserbuf b;
    x10rt_deserbuf_init(&b, p);
    x10rt_remote_ptr finish_counter_; x10rt_deserbuf_read(&b, &finish_counter_);

    int *finish_counter = (int*)(size_t) finish_counter_;
    (*finish_counter)--;
}

static void recv_print(const x10rt_msg_params *p)
{
    x10rt_deserbuf b;
    x10rt_deserbuf_init(&b, p);
    int32_t i; x10rt_deserbuf_read(&b, &i);
    x10rt_team team; x10rt_deserbuf_read(&b, &team);
    x10rt_place home; x10rt_deserbuf_read(&b, &home);
    x10rt_remote_ptr finish_counter_; x10rt_deserbuf_read(&b, &finish_counter_);

    std::cout << team << ": " << std::string(i,' ') << '\\' << std::endl;
    
    x10rt_serbuf b2;
    x10rt_serbuf_init(&b2, home, FINISH_ID);
    x10rt_serbuf_write(&b2, &finish_counter_);
    x10rt_net_send_msg(&b2.p);
    x10rt_serbuf_free(&b2);
}   


static void coll_test (x10rt_team team, x10rt_place role)
{
    int finished;
    const int tests = 10000;
    unsigned long long taken;

    {
        if (0==role)
            std::cout << team << ": barrier sync test (shape should be \\):  " << std::endl;
        for (x10rt_place i=0 ; i<x10rt_team_sz(team) ; ++i) {
            if (i==role) {
                if (i==0) {
                    std::cout << team << ": " << std::string(i,' ') << '\\' << std::endl;
                } else {
                    x10rt_place home = x10rt_here();
                    int finish_counter = 1;
                    x10rt_remote_ptr finish_counter_ = (x10rt_remote_ptr)(size_t)&finish_counter;
                    x10rt_serbuf b;
                    x10rt_serbuf_init(&b, 0, PRINT_ID);
                    x10rt_serbuf_write(&b, &i);
                    x10rt_serbuf_write(&b, &team);
                    x10rt_serbuf_write(&b, &home);
                    x10rt_serbuf_write(&b, &finish_counter_);
                    x10rt_net_send_msg(&b.p);
                    x10rt_serbuf_free(&b);
                    while (finish_counter) { x10rt_probe(); }
                }
            }
            x10rt_barrier_b(team,role);
        }

        if (0==role) std::cout << team << ": barrier timing..." << std::endl;
        x10rt_barrier_b(team,role);
        taken = -nano_time();
        for (int i=0 ; i<tests ; ++i) {
            finished = 0;
            x10rt_barrier(team, role, x10rt_one_setter, &finished);
            while (!finished) { sched_yield(); x10rt_probe(); }
        }
        taken += nano_time();
        if (0==role) std::cout << team << ": barrier time:  "
                               << ((double)taken)/tests/1000 << " μs" << std::endl;
    }

    {
        x10rt_place root = 43 % x10rt_team_sz(team);
        float sbuf[113];
        float dbuf[113];
        size_t el = sizeof(float);
        size_t count = sizeof(sbuf)/sizeof(*sbuf);

        for (size_t i=0 ; i<count ; ++i) sbuf[i] = float(role) * i * i + 1;
        for (size_t i=0 ; i<count ; ++i) dbuf[i] = -(float)i;

        if (0==role)
            std::cout << team << ": bcast from " << root
                      << " correctness (if no warnings follow then OK)..." << std::endl;
        finished = 0;
        x10rt_bcast(team, role, root, root==role ? sbuf : NULL, dbuf,
                    el, count, x10rt_one_setter, &finished);
        while (!finished) { x10rt_probe(); }
        for (size_t i=0 ; i<count ; ++i) {
            float oracle = float(root) * i * i + 1;
            if (dbuf[i] != oracle) {
                std::cout << team << ": role " << role
                          << " has received invalid data from bcast: ["<<i<<"] = " << dbuf[i]
                          << " (not " << oracle << ")" << std::endl;
            }
        }

        if (0==role) std::cout << team << ": bcast timing test..." << std::endl;
        x10rt_barrier_b(team,role);
        taken = -nano_time();
        for (int i=0 ; i<tests ; ++i) {
            finished = 0;
            x10rt_bcast(team, role, root, sbuf, dbuf, el, count, x10rt_one_setter, &finished);
            while (!finished) { sched_yield(); x10rt_probe(); }
        }
        taken += nano_time();
        if (0==role) std::cout << team << ": bcast time:  "
                               << ((double)taken)/tests/1000 << " μs" << std::endl;
    }

    {
        x10rt_place root = 43 % x10rt_team_sz(team);
        size_t count = 123;
        typedef double test_t;
        test_t *sbuf = new test_t[count*x10rt_team_sz(team)];
        size_t el = sizeof(test_t);
        test_t *dbuf = new test_t[count*x10rt_team_sz(team)];

        for (size_t p=0 ; p<x10rt_team_sz(team) ; ++p) {
            for (size_t i=0 ; i<count ; ++i) {
                sbuf[p*count + i] = pow(test_t(p+2),test_t(role+1)) + i;
            }
        }
        for (size_t i=0 ; i<count*x10rt_team_sz(team) ; ++i) dbuf[i] = -(test_t)i;

        if (0==role)
            std::cout << team << ": scatter from " << root
                      << " correctness (if no warnings follow then OK)..." << std::endl;
        finished = 0;
        x10rt_scatter(team, role, root, root==role ? sbuf : NULL, dbuf,
                      el, count, x10rt_one_setter, &finished);
        while (!finished) { x10rt_probe(); }
        for (size_t i=0 ; i<count ; ++i) {
            test_t oracle = pow(test_t(role+2),test_t(root+1)) + i;
            if (dbuf[i] != oracle) {
                std::cout << team << ": role " << role
                          << " has received invalid data from scatter: ["<<i<<"] = " << dbuf[i]
                          << " (not " << oracle << ")" << std::endl;
            }
        }

        if (0==role) std::cout << team << ": scatter timing test..." << std::endl;
        x10rt_barrier_b(team,role);
        taken = -nano_time();
        for (int i=0 ; i<tests ; ++i) {
            finished = 0;
            x10rt_scatter(team, role, root, sbuf, dbuf, el, count, x10rt_one_setter, &finished);
            while (!finished) { sched_yield(); x10rt_probe(); }
        }
        taken += nano_time();
        if (0==role) std::cout << team << ": scatter time:  "
                               << ((double)taken)/tests/1000 << " μs" << std::endl;
    }

    {
        size_t count = 123;
        typedef double test_t;
        test_t *sbuf = new test_t[count*x10rt_team_sz(team)];
        size_t el = sizeof(test_t);
        test_t *dbuf = new test_t[count*x10rt_team_sz(team)];

        for (size_t p=0 ; p<x10rt_team_sz(team) ; ++p) {
            for (size_t i=0 ; i<count ; ++i) {
                sbuf[p*count + i] = pow(test_t(p+2),test_t(role+1)) + i;
            }
        }
        for (size_t i=0 ; i<count*x10rt_team_sz(team) ; ++i) dbuf[i] = -(test_t)i;

        if (0==role)
            std::cout<<team<<": alltoall correctness (if no errors then OK):" << std::endl;
        finished = 0;
        x10rt_alltoall(team, role, sbuf, dbuf, el, count,x10rt_one_setter, &finished);
        while (!finished) { x10rt_probe(); }
        for (size_t p=0 ; p<x10rt_team_sz(team) ; ++p) {
            for (size_t i=0 ; i<count ; ++i) {
                test_t oracle = pow(test_t(role+2),test_t(p+1)) + i;
                if (dbuf[p*count + i] != oracle) {
                    std::cout << team << ": role " << role
                              << " has received invalid data from " << p
                              << ": ["<<i<<"] = " << dbuf[p*count+i]
                              << " (not " << oracle << ")" << std::endl;
                }
            }
        }


        if (0==role) std::cout << team << ": alltoall timing test..." << std::endl;
        x10rt_barrier_b(team,role);
        taken = -nano_time();
        for (int i=0 ; i<tests ; ++i) {
            finished = 0;
            x10rt_alltoall(team, role, sbuf, dbuf, el, count, x10rt_one_setter, &finished);
            while (!finished) { sched_yield(); x10rt_probe(); }
        }
        taken += nano_time();
        if (0==role) std::cout << team << ": alltoall time:  "
                               << ((double)taken)/tests/1000 << " μs" << std::endl;

        delete [] sbuf;
        delete [] dbuf;
    }

    {
        float sbuf[113];
        float dbuf[113];
        size_t count = sizeof(sbuf)/sizeof(*sbuf);

        for (size_t i=0 ; i<count ; ++i) sbuf[i] = float(role+1) * i * i;
        for (size_t i=0 ; i<count ; ++i) dbuf[i] = -(float)i;

        if (0==role)
            std::cout<<team<<": allreduce correctness (if no errors then OK):" << std::endl;
        finished = 0;
        x10rt_allreduce(team, role, sbuf, dbuf, X10RT_RED_OP_ADD, X10RT_RED_TYPE_FLT, count,
                            x10rt_one_setter, &finished);
        while (!finished) { x10rt_probe(); }
        float oracle_base = (x10rt_team_sz(team)*x10rt_team_sz(team) + x10rt_team_sz(team))/2;
        for (size_t i=0 ; i<count ; ++i) {
            float oracle = oracle_base * i * i;
            if (dbuf[i] != oracle) {
                std::cout << team << ": role " << role
                          << " has received invalid sum at ["<<i<<"]:  " << dbuf[i]
                          << " (not " << oracle << ")" << std::endl;
            }
        }


        if (0==role) std::cout << team << ": allreduce timing test..." << std::endl;
        x10rt_barrier_b(team,role);
        taken = -nano_time();
        for (int i=0 ; i<tests ; ++i) {
            finished = 0;
            x10rt_allreduce(team, role, sbuf, dbuf, X10RT_RED_OP_ADD, X10RT_RED_TYPE_FLT, count,
                                x10rt_one_setter, &finished);
            while (!finished) { sched_yield(); x10rt_probe(); }
        }
        taken += nano_time();
        if (0==role) std::cout << team << ": allreduce time:  "
                               << ((double)taken)/tests/1000 << " μs" << std::endl;
    }

}

static void spmd_test (x10rt_team team, x10rt_place role)
{

    if (0==role) {
        std::cout << "----------------" << std::endl;
        std::cout << "Basic SPMD tests" << std::endl;
        std::cout << "----------------" << std::endl;
    }

    coll_test(team, role);

    if (0==role) std::cout << std::endl;

    if (0==role) std::cout << team << ": Split...  " << std::endl;
    x10rt_team odds_n_evens = 0;
    x10rt_place colour = role%2;
    x10rt_place new_role = role/2;
    x10rt_team_split(team, role, colour, new_role, x10rt_team_setter, &odds_n_evens);
    while (odds_n_evens == 0) x10rt_probe();
    if (0==role) std::cout << "Evens team is:  " << odds_n_evens << std::endl;
    if (1==role) std::cout << "Odds team is:  " << odds_n_evens << std::endl;

    x10rt_barrier_b(team,role); // wait for everyone to print

    coll_test(odds_n_evens, new_role);

    x10rt_barrier_b(team,role); // wait for everyone to finish

    if (0==role) std::cout << std::endl;

    x10rt_barrier_b(team,role); // wait for place 0 to finish printing

    if (0==role || 1==role) std::cout << odds_n_evens << ": Destroying team... " << std::endl;
    int finished = 0;
    x10rt_team_del(odds_n_evens, new_role, x10rt_one_setter, &finished);
    while (!finished) x10rt_probe();
    if (0==role || 1==role) std::cout << odds_n_evens << ": Destroyed team. " << std::endl;

    x10rt_barrier_b(team,role); // wait for everyone to finish

    if (0==role) std::cout << std::endl;

    x10rt_barrier_b(team,role); // wait for place 0 to finish printing

}

static void recv_test(const x10rt_msg_params *p)
{
    x10rt_deserbuf b;
    x10rt_deserbuf_init(&b, p);
    x10rt_team team; x10rt_deserbuf_read(&b, &team);
    x10rt_place role; x10rt_deserbuf_read(&b, &role);

    spmd_test(team, role);

    int finished = 0;
    x10rt_team_del(team, role, x10rt_one_setter, &finished);
    while (!finished) x10rt_probe();

    time_to_quit--;
}

static void apgas_test (void)
{
    std::cout << std::endl;
    std::cout << "----------------------" << std::endl;
    std::cout << "Explicit Team creation" << std::endl;
    std::cout << "----------------------" << std::endl;
    std::cout << "Creating team...  " << std::endl;
    x10rt_place *memberv = new x10rt_place [x10rt_net_nhosts()];
    for (x10rt_place i=0 ; i<x10rt_nplaces() ; ++i) {
        memberv[i] = i;
    }
    x10rt_team nu_team = 0;
    x10rt_team_new(x10rt_nplaces(), memberv, x10rt_team_setter, &nu_team);
    while (nu_team == 0) x10rt_probe();
    delete [] memberv;
    std::cout << "New team is:  " << nu_team << std::endl;

    for (x10rt_place i=1 ; i<x10rt_nplaces() ; ++i) {
        x10rt_serbuf b;
        x10rt_serbuf_init(&b, i, TEST_ID);
        x10rt_serbuf_write(&b, &nu_team);
        x10rt_serbuf_write(&b, &i);
        x10rt_net_send_msg(&b.p);
        x10rt_serbuf_free(&b);
    }
    spmd_test(nu_team, 0);
    std::cout << nu_team << ": Destroying team...  " << std::endl;
    int finished = 0;
    x10rt_team_del(nu_team, 0, x10rt_one_setter, &finished);
    while (!finished) x10rt_probe();
    std::cout << nu_team << ": Destroyed team.  " << std::endl;
}

int main (int argc, char **argv)
{
    x10rt_init(&argc, &argv);
    TEST_ID = x10rt_register_msg_receiver(&recv_test,NULL,NULL,NULL,NULL);
    PRINT_ID = x10rt_register_msg_receiver(&recv_print,NULL,NULL,NULL,NULL);
    FINISH_ID = x10rt_register_msg_receiver(&recv_finish,NULL,NULL,NULL,NULL);

    x10rt_registration_complete();

    if (0==x10rt_here()) {
        std::cout << "===========================" << std::endl;
        std::cout << "X10RT Collective test suite" << std::endl;
        std::cout << "Number of places:  " << x10rt_nplaces() << std::endl;
        std::cout << "Size of world:  " << x10rt_team_sz(0) << std::endl;
        std::cout << "===========================" << std::endl << std::endl;;
        std::cout << std::endl;
    }

    spmd_test(0, x10rt_here());
    
    spmd_test(0, x10rt_here());
    
    if (0==x10rt_here()) {
        apgas_test();
        apgas_test();
    } else {
        time_to_quit = 2;
        while (time_to_quit != 0) x10rt_probe();
    }

    x10rt_finalize();

    return EXIT_SUCCESS;
}

// vim: shiftwidth=4:tabstop=4:expandtab

